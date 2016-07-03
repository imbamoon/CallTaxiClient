package com.zyw.driver;

import android.app.Application;

import com.baidu.mapapi.BMapManager;

/**
 * Created by zyw on 2016/5/19.
 */
public class GateApplication extends Application {

    public String mk = "";
    public String prefix = "";
    public String ip = "";
    public int number = 0;
    public double aimLati, aimLong;
    public String strAddr = null;
    public String id = null;//用户名
    int log_id = 0;

    @Override
    public void onCreate() {
        setIp();
        setNumber();
        setMk();
        setPrefix();
        super.onCreate();
    }
    public void setIp(){
        ip = "192.168.1.187";
    }
    public void setNumber(){
        number = 8000;
    }
    public void setMk() {
        mk = "cGpmd8s4Y4yNGNjXqGScS8oXluLPCG3F";//百度地图key
    }
    public void setPrefix() {
        prefix = "http://"+ip+":"+number+"/";//ip地址和端口号
    }
}
