package com.zyw.driver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends Activity implements View.OnClickListener {

    public Button btnRegisterBack, btnFinishRegister;
    public EditText inputRegisterId, inputRegisterPwd, inputRegisterRePwd;
    public String prefix;
    public GateApplication app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        app = (GateApplication)getApplication();
        prefix = app.prefix;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnRegisterBack = (Button)findViewById(R.id.register_back_button);
        btnFinishRegister = (Button)findViewById(R.id.finish_register_button);
        inputRegisterId = (EditText)findViewById(R.id.register_username);
        inputRegisterPwd = (EditText)findViewById(R.id.register_psword);
        inputRegisterRePwd = (EditText)findViewById(R.id.register_repsword);

        btnRegisterBack.setOnClickListener(this);
        btnFinishRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.register_back_button:
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                break;
            case R.id.finish_register_button:
                final String inputUsername = inputRegisterId.getText().toString();
                String inputPwd = inputRegisterPwd.getText().toString();
                String inputRePwd = inputRegisterRePwd.getText().toString();

                if (inputUsername.equals("")||inputPwd.equals("")||inputRePwd.equals("")){
                    new AlertDialog.Builder(RegisterActivity.this).setMessage("不能为空")
                            .setPositiveButton("确定", null)
                            .setCancelable(true)
                            .show();
                    break;
                }else if (!inputPwd.equals(inputRePwd)){
                    new AlertDialog.Builder(RegisterActivity.this).setMessage("两次密码不一致")
                            .setPositiveButton("确定", null)
                            .setCancelable(true)
                            .show();
                    break;
                }

                String url = prefix + "register/driver";
                JSONObject userInfo = new JSONObject();
                try {
                    userInfo.put("username", inputUsername);
                    userInfo.put("pwd", inputPwd);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new AsyncTask<Object, Void, String>(){

                    @Override
                    protected String doInBackground(Object... params) {
                        try {
                            String ret = new HttpFunc().execute((String) params[0], (JSONObject) params[1]);
                            return ret;

                        } catch (JSONException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        JSONObject jsonObject;
                        String result = "";
                        try {
                            jsonObject = new JSONObject(s);
                            result = jsonObject.getString("result");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if(result.equals("exist")) {
                            new AlertDialog.Builder(RegisterActivity.this).setMessage("您的帐号已经存在,请直接登陆")
                                    .setPositiveButton("确定", null)
                                    .setCancelable(true)
                                    .show();
                        } else if (result.equals("success")){
                            new AlertDialog.Builder(RegisterActivity.this).setMessage("注册成功")
                                    .setPositiveButton("确定", null)
                                    .setCancelable(true)
                                    .show();
                            app.id = inputUsername;
                            startActivity(new Intent(RegisterActivity.this, MapActivity.class));
                        }else{
                            new AlertDialog.Builder(RegisterActivity.this).setMessage("未知错误")
                                    .setPositiveButton("确定", null)
                                    .setCancelable(true)
                                    .show();
                        }
                    }
                }.execute(url, userInfo);
        }
    }
}
