package com.zyw.calltaxi;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by zyw on 2016/5/18.
 */
public class HttpFunc {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static String execute(String url) {return "1";}//TODO
    public String execute(String url,JSONObject jsonObject) throws JSONException {
        String result = "";
        OkHttpClient client = new OkHttpClient();
        String json = jsonObject.toString();
        RequestBody requestBody = RequestBody.create(JSON,json);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                result = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
