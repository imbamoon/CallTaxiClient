package com.zyw.driver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class StatusActivity extends Activity implements View.OnClickListener {

    public String prefix;
    public GateApplication app;
    public TextView status;
    public String webRet;
    final Handler handle = new Handler();
    final Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            if (webRet.equals("cancel"))
                status.setText("这个订单居然取消了诶");
            else if (webRet.equals("still"))
                status.setText("请火速赶往现场");
            else {
                Toast toast = Toast.makeText(StatusActivity.this, "该订单已完成", Toast.LENGTH_LONG);
                toast.show();
                startActivity(new Intent(StatusActivity.this, MapActivity.class));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        app = (GateApplication) getApplication();
        prefix = app.prefix;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        Button btnCancel = (Button) findViewById(R.id.btn_cancel);//取消订单
        btnCancel.setOnClickListener(this);

        status = (TextView) findViewById(R.id.status_Text);
        TextView getCar = (TextView) findViewById(R.id.getCar_text);
        getCar.setText("您的接客地点为:" + app.strAddr);

        String urlCommit = prefix + "updateLog.php?driver_id=" + app.id + "&log_id=" + app.log_id;
        String ret = new HttpFunc().execute(urlCommit);
        app.log_id = Integer.parseInt(ret);

        new Thread(new Update()).start();
    }

    public class Update implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(2000);
                    String url = prefix + "UpdateStatus.php?log_id=" + app.log_id;//看司机是否接单，更新状态
                    webRet = new HttpFunc().execute(url);
                    handle.post(updateUI);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        String urlCancel = prefix + "update.php?log_id=" + app.log_id + "&to=-1";
        new HttpFunc().execute(urlCancel);
        startActivity(new Intent(StatusActivity.this, MapActivity.class));
    }
}

