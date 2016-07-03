package com.zyw.calltaxi.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.content.Intent;

import com.zyw.calltaxi.GateApplication;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocketService extends Service {

    private String positionDriver;
    private Callback callback;
    private Socket socket;
    private GateApplication app;
    private String ip;
    private int number;
    private BufferedReader reader;
    private BufferedWriter writer;

    public SocketService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        app = (GateApplication)getApplication();
        ip = app.ip;
        number = app.number;
        //从服务端获取positionDriver
        new Thread(){
            @Override
            public void run() {
                super.run();

                try {
                    socket = new Socket(ip,number);
                    String line;
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
                    Pattern pattern = Pattern.compile("\\d{2,3}\\.\\d{6}.*");
                    Matcher matcher;
                    while ((line = reader.readLine())!=null){
                        positionDriver = line;
                        matcher = pattern.matcher(positionDriver);//确保positionDriver符合格式
                        if (callback!=null&&matcher.find()){
                            callback.onReceiveDriverPosition(positionDriver);
                        }
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class Binder extends android.os.Binder{
//        public void sendPositionUser(String positon){
//            try {
//                writer.write(positon+"\n");
//                writer.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        public SocketService getService(){
            return SocketService.this;
        }
    }

    public void setCallback(Callback callback){
        this.callback = callback;
    }

    public interface Callback{
        void onReceiveDriverPosition(String position);
    }
}
