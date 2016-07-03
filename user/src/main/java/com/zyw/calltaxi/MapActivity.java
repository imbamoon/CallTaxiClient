package com.zyw.calltaxi;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
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
import com.zyw.calltaxi.service.SocketService;

import java.lang.ref.WeakReference;

public class MapActivity extends Activity implements BDLocationListener, View.OnClickListener, ServiceConnection {

    //地图基础变量
    private MapView mMapView;
    private BaiduMap mBDMap;
    //定位
    private LocationClient mLocClient;
    //是否第一次定位
    private boolean isFirstLoc = true;
    private SocketService.Binder mbinder;
    public GateApplication app;
    public String prefix;
    private MyHandler myHandler;
    private String positionMe;
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        app = (GateApplication) getApplication();
        prefix = app.prefix;
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        //初始化地图和提交按钮
        initView();
        //定位
        location();
        //绑定服务
        bindSocketService();
//        new AsyncTask<String,>() {
//            @Override
//            protected Object doInBackground(Object[] params) {
//                return null;
//            }
//        }

//        new Thread(new Update()).start();//更新周围司机位置
    }

    private void bindSocketService() {

        Intent intent = new Intent(MapActivity.this, SocketService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

//    public class Update implements Runnable {
//        @Override
//        public void run() {
//            Looper.prepare();
//            while (true) {
//                try {
//                    Thread.sleep(2000);
//                    mMapView.removeAllViews();
//
//                    String url = prefix + "getDriver.php";
//                    String ret = new HttpFunc().execute(url);
//                    JSONArray json = new JSONArray(ret);
//
//                    double lati = 0;
//                    double longi = 0;
//                    for (int i = 0; i < json.length(); i++) {
//                        double value = json.getDouble(i);
//                        if (0 == i % 2)
//                            lati = value;
//                        else {
//                            longi = value;
//                            //定义Maker坐标点
//                            LatLng point = new LatLng(lati, longi);
//                            //构建Marker图标
//                            BitmapDescriptor bitmap = BitmapDescriptorFactory
//                                    .fromResource(R.drawable.icon_marka);
//                            //构建MarkerOption，用于在地图上添加Marker
//                            OverlayOptions option = new MarkerOptions()
//                                    .position(point)
//                                    .icon(bitmap);
//                            //在地图上添加Marker，并显示
//                            mBDMap.addOverlay(option);
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    /**
     * 提交按钮点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (app.id==null){
            startActivity(new Intent(MapActivity.this,LoginActivity.class));
            return;
        }
        LatLng latLng = mBDMap.getMapStatus().target;
        app.aimLati = latLng.latitude;
        app.aimLong = latLng.longitude;
        startActivity(new Intent(MapActivity.this, CommitActivity.class));
    }

    /**
     * 初始化地图和提交按钮
     */
    private void initView() {
        //地图初始化
        mMapView = (MapView) findViewById(R.id.bmapsView);
        mBDMap = mMapView.getMap();
        //开启定位图层
        mBDMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(17).build()));
        mBDMap.setMyLocationEnabled(true);
        //提交按钮
        Button btnCommit = (Button) findViewById(R.id.CommitButton);
        btnCommit.setOnClickListener(this);
        //实例化myHandler
        myHandler = new MyHandler(this);
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
        //给经纬度赋值
        positionMe = Double.toString(bdLocation.getLatitude()) + "," + Double.toString(bdLocation.getLongitude())+",user,"+app.id;//构造字符串，
//        //将positionMe传给service
//        if (mbinder != null) {
//            mbinder.sendPositionUser(positionMe);
//        }
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

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, R.string.exit_app, Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        //用Binder对象与服务器连接
        mbinder = (SocketService.Binder) service;

        //从service获取司机位置
        if (mbinder != null) {
            mbinder.getService().setCallback(new SocketService.Callback() {
                @Override
                public void onReceiveDriverPosition(String position) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();

                    bundle.putString("position", position);
                    message.setData(bundle);
                    myHandler.sendMessage(message);
                }
            });
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mbinder = null;
    }

    private static class MyHandler extends Handler {

        WeakReference<MapActivity> mapActivityWeakReference;

        MyHandler(MapActivity mapActivity) {
            mapActivityWeakReference = new WeakReference<>(mapActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg != null) {
                String positionDriver = msg.getData().getString("position");
                String[] str = positionDriver.split(",");//用逗号将经纬度分开
                mapActivityWeakReference.get().mBDMap.clear();
                //定义Marker坐标点
                LatLng driverPoint = new LatLng(Double.parseDouble(str[0]), Double.parseDouble(str[1]));
                //构建Marker图标
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions().position(driverPoint).icon(bitmapDescriptor);
                //在地图上添加Marker，并显示
                mapActivityWeakReference.get().mBDMap.addOverlay(option);
            }
        }
    }
}
