package com.zyw.driver.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zyw.driver.GateApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by zyw on 2016/7/3.
 */
public class SocketService extends Service {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private GateApplication app;
    private String ip;
    private int number;
    private CallBack callBack;

    @Override
    public void onCreate() {
        super.onCreate();

        app = (GateApplication)getApplication();
        ip = app.ip;
        number = app.number;
        new Thread(){
            @Override
            public void run() {
                super.run();

                try {
                    socket = new Socket(ip,number);
                    String line;
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
                    while((line=reader.readLine())!=null){

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    public class Binder extends android.os.Binder{

        public void sendPositionDriver(String driverName,String position){
            try {
                writer.write(driverName+","+position+"\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public SocketService getService(){
            return SocketService.this;
        }
    }

    public interface CallBack{
        void onReceiveUserOrder(String username,String userPosition,String destination);
    }

    public void setCallback(CallBack callBack){
        this.callBack = callBack;
    }
}
