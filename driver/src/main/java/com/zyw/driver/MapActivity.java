package com.zyw.driver;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.inner.GeoPoint;

import org.json.JSONArray;

public class MapActivity extends Activity implements BDLocationListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    //地图基础变量
    private MapView mMapView;
    private BaiduMap mBDMap;
    //定位
    private LocationClient mLocClient;
    //是否第一次定位
    private boolean isFirstLoc = true;
    public GateApplication app;
    public String prefix;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        app = (GateApplication) getApplication();
        prefix = app.prefix;
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        //初始化地图
        initView();
        //定位
        location();
        //接客中、等待中状态切换
        Switch swt = (Switch)findViewById(R.id.swt);
        swt.setChecked(isWork());
        swt.setOnCheckedChangeListener(this);
        //提交按钮
        Button btnCommit = (Button) findViewById(R.id.CommitButton);
        btnCommit.setOnClickListener(this);

        new Thread(new Update()).start();//更新周围乘客位置
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Toast.makeText(MapActivity.this, isChecked + "", Toast.LENGTH_SHORT).show();
        HttpFunc web = new HttpFunc();
        String url = prefix + "changeDriver.php?id=" + app.id + "&type=";
        if(isChecked)
            url = url + "1";//接客中
        else
            url = url + "0";//等待中
        web.execute(url);
    }

    public class Update implements Runnable {
        @Override
        public void run() {
            Looper.prepare();
            while (true) {
                try {
                    Thread.sleep(2000);
                    mMapView.removeAllViews();

                    String url = prefix + "getUser.php";
                    String ret = new HttpFunc().execute(url);
                    JSONArray json = new JSONArray(ret);

                    double value,lati = 0,longi = 0;
                    int log_id;
                    for(int i = 0 ;i < json.length(); i++) {
                        if(2 == i % 3) {
                            log_id = json.getInt(i);
                            app.log_id = log_id;
                            continue;
                        }
                        value = json.getDouble(i);
                        if(0 == i % 3)
                            lati = value;
                        else if(1 == i % 3) {
                            longi = value;
                            //定义Maker坐标点
                            LatLng point = new LatLng(lati, longi);
                            //构建Marker图标
                            BitmapDescriptor bitmap = BitmapDescriptorFactory
                                    .fromResource(R.drawable.icon_marka);
                            //构建MarkerOption，用于在地图上添加Marker
                            OverlayOptions option = new MarkerOptions()
                                    .position(point)
                                    .icon(bitmap);
                            //在地图上添加Marker，并显示
                            mBDMap.addOverlay(option);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 判断司机是否在工作状态
     * @return
     */
    public boolean isWork() {
        HttpFunc web = new HttpFunc();
        String url = prefix + "isWork.php?id=" + app.id;
        String ret = web.execute(url);
        if(ret.equals("yes"))
            return true;
        return false;
    }

    @Override
    public void onClick(View v) {
        LatLng latLng = mBDMap.getMapStatus().target;
        app.aimLati = latLng.latitude;
        app.aimLong = latLng.longitude;
        startActivity(new Intent(MapActivity.this, CommitActivity.class));
    }

    /**
     * 初始化地图
     */
    private void initView() {
        //地图初始化
        mMapView = (MapView) findViewById(R.id.bmapsView);
        mBDMap = mMapView.getMap();
        //开启定位图层
        mBDMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(17).build()));
        mBDMap.setMyLocationEnabled(true);
    }

    /**
     * 定位
     */
    private void location() {
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(this);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//打开GPS
        option.setCoorType("bd09ll");//设置坐标类型
        option.setScanSpan(1000);//设置请求间隔时间
        mLocClient.setLocOption(option);//加载配置
        mLocClient.start();//开始定位
    }

    /**
     * 定位监听
     *
     * @param bdLocation
     */
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        //如果bdLocation为空或mapView销毁后不再处理新数据接收的位置
        if (bdLocation == null || mMapView == null) {
            return;
        }

        //构造定位数据
        MyLocationData data = new MyLocationData.Builder()
                //精度（半径）
                .accuracy(bdLocation.getRadius())
                        //此处设置开发者获取到的方向信息，顺时针0-360
                .direction(0)
                .latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude()).build();

        //设置定位数据
        mBDMap.setMyLocationData(data);

        //是否是第一次定位
        if (isFirstLoc) {
            isFirstLoc = false;
            LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            //地图状态更新
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(ll);
            mBDMap.animateMapStatus(msu);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();


        mLocClient.stop();
        //关闭定位图层
        mBDMap.setMyLocationEnabled(false);

        mMapView.onDestroy();
        mMapView = null;
    }

}
