package com.zyw.calltaxi;

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

public class LoginActivity extends Activity implements View.OnClickListener {

    public EditText inputUsername, inputPwd;
    public Button btnLogin, btnRegister;
    public GateApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        app = (GateApplication) getApplication();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = (Button) findViewById(R.id.login_button);
        btnRegister = (Button) findViewById(R.id.register_button);
        inputUsername = (EditText) findViewById(R.id.login_username);
        inputPwd = (EditText) findViewById(R.id.login_psword);

        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
    }

//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        app = (GateApplication) getApplication();
//        if (app.id != null) {
//            startActivity(new Intent(LoginActivity.this, MapActivity.class));
//        }
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button:
                final String Susername = inputUsername.getText().toString();
                String Spwd = inputPwd.getText().toString();

                if (Susername.equals("") || Spwd.equals("")) {
                    new AlertDialog.Builder(LoginActivity.this).setMessage("不能为空")
                            .setPositiveButton("确定", null)
                            .setCancelable(true)
                            .show();
                    break;
                }

                JSONObject userInfo = new JSONObject();


                try {
                    userInfo.put("username", Susername);
                    userInfo.put("pwd", Spwd);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String url = app.prefix + "login/user";

                new AsyncTask<Object, Void, String>() {
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
                        if (result.equals("yes")) {
                            app.id = Susername;
                            startActivity(new Intent(LoginActivity.this, MapActivity.class));
                        } else {
                            new AlertDialog.Builder(LoginActivity.this).setMessage("用户名或密码错误")
                                    .setPositiveButton("确定", null)
                                    .setCancelable(true)
                                    .show();
                        }
                    }
                }.execute(url, userInfo);

                break;
            case R.id.register_button:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                break;
        }
    }
}
