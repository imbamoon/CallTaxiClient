package com.zyw.driver;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

public class CommitActivity extends Activity implements View.OnClickListener, OnGetGeoCoderResultListener {

    public GateApplication app;
    public TextView address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        app = (GateApplication) getApplication();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commit);

        Button btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);

        double aimLati = app.aimLati;
        double aimLong = app.aimLong;
        LatLng latLng = new LatLng(aimLati,aimLong);
        address = (TextView)findViewById(R.id.address);
        // 创建地理编码检索实例
        GeoCoder geoCoder = GeoCoder.newInstance();
        // 设置地理编码检索监听者
        geoCoder.setOnGetGeoCodeResultListener(this);
        // 反向地理编码
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
        // 释放地理编码检索实例
        geoCoder.destroy();
    }

    @Override
    public void onClick(View v) {
        new AlertDialog.Builder(CommitActivity.this).setMessage("确认提交订单")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(CommitActivity.this, StatusActivity.class));
                    }
                })
                .setNegativeButton("取消", null)
                .setCancelable(true)
                .show();
    }

    /**
     * 正向地理编码实现
     * @param result
     */
    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {

    }

    /**
     * 反向地理编码实现
     * @param result
     */
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            // 没有检测到结果
            Toast.makeText(CommitActivity.this, "抱歉，未能找到结果",
                    Toast.LENGTH_LONG).show();
        }
        if (result != null){
            address.setText(result.getAddress());
            app.strAddr = result.getAddress();
            Toast.makeText(CommitActivity.this, result.getAddress(), Toast.LENGTH_LONG).show();
        }
    }
}
