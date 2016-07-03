package com.zyw.calltaxi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StatusActivity extends Activity implements View.OnClickListener {

    public String prefix;
    public GateApplication app;
    public TextView status;
    public String webRet;
    final Handler handle = new Handler();
    final Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            if(webRet.equals("no"))
                status.setText("暂时还没有司机来接您呦");
            else
                status.setText(String.format("牌照为%s的车辆已经接单",webRet));
            Log.e("commit", "updateUI");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        app = (GateApplication)getApplication();
        prefix = app.prefix;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        Button btnCancel = (Button)findViewById(R.id.btn_cancel);//取消订单
        btnCancel.setOnClickListener(this);
        Button btnFinish = (Button)findViewById(R.id.btn_finish);
        btnFinish.setOnClickListener(this);

        status = (TextView)findViewById(R.id.status_Text);
        status.setText("暂时还没有司机来接您哦");
        TextView getCar = (TextView)findViewById(R.id.getCar_text);
        getCar.setText("您的上车地点为:" + app.strAddr);

        String urlCommit = prefix + "Commit.php?id=" + app.id + "&lati=" + app.aimLati + "&long=" + app.aimLong;//用户提交订单，上传自己id和位置
        String ret = new HttpFunc().execute(urlCommit);
        app.log_id = Integer.parseInt(ret);

        new Thread(new Update()).start();
    }

    public class Update implements Runnable {
        @Override
        public void run() {
            try {
                while(true) {
                    Thread.sleep(2000);
                    String url = prefix + "UpdateStatus.php?log_id=" + app.log_id;//看司机是否接单，更新状态
                    webRet = new HttpFunc().execute(url);
                    handle.post(updateUI);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_cancel:
                String urlCancel = prefix + "update.php?log_id=" + app.log_id + "&to=-1";
                new HttpFunc().execute(urlCancel);
                startActivity(new Intent(StatusActivity.this, MapActivity.class));
                break;
            case R.id.btn_finish:
                String urlFinish = prefix + "update.php?log_id=" + app.log_id + "&to=2";
                new HttpFunc().execute(urlFinish);
                startActivity(new Intent(StatusActivity.this, MapActivity.class));
                break;
        }
    }
}
