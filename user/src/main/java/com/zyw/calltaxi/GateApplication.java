package com.zyw.calltaxi;

import android.app.Application;

/**
 * Created by zyw on 2016/5/17.
 */
public class GateApplication extends Application {
    public String mk = "";
    public String prefix = "";
    public double aimLati, aimLong;
    public String strAddr = null;
    public String id = null;//用户名
    public String ip = "192.168.1.104";
    public int number = 8080;
    int log_id = 0;

    @Override
    public void onCreate() {
        setMk();
        setPrefix();
        super.onCreate();
    }

    public void setMk() {
        mk = "cGpmd8s4Y4yNGNjXqGScS8oXluLPCG3F";//百度地图key
    }

    public void setPrefix() {
        prefix = "http://"+ip+":"+number+"/";//ip地址和端口号
    }
}
