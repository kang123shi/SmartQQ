package com.fanbo.taokehelper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.fanbo.taokehelper.Activity.QQMessageActivity;
import com.fanbo.taokehelper.Application.MyApplication;
import com.scienjus.smartqq.client.QQClient;
import com.scienjus.smartqq.model.UserInfo;

import net.dongliu.requests.Session;


import java.util.regex.Matcher;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {
    private static final String LOG ="MainActivity";
    @BindView(R.id.iv_code)
    ImageView ivCode;
    @BindView(R.id.btn_startlogin)
    Button btnStartlogin;
    //发生ngnix 404 时的重试次数
    private String qrsig;

    //会话
    private Session session;
    private static final String TAG ="MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        btnStartlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
//                Intent intent = new Intent(MainActivity.this, QQMessageActivity.class);
//                MainActivity.this.startActivity(intent);
            }
        });
    }
    private void login() {
        QQClient.getInstance().getQRCode(new QQClient.Listener() {
            @Override
            public void success(final Object object) {
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             Bitmap bitmap =(Bitmap) object;
                             ivCode.setImageBitmap(bitmap);
                         }
                     });

                QQClient.getInstance().checkVCode(new QQClient.CheckErCodeListener() {

                    @Override
                    public void checking(String status) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "二维码正在认证中", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void success(Object object) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                QQClient.getInstance().getAccount(new QQClient.Listener() {
                                    @Override
                                    public void success(Object object) {
                                        UserInfo uerInfo = (UserInfo) object;
                                        if (uerInfo!=null){
                                            MyApplication.getInstance().userInfo = uerInfo;
                                            Intent intent = new Intent(MainActivity.this, QQMessageActivity.class);
                                            MainActivity.this.startActivity(intent);
                                        }
                                    }

                                    @Override
                                    public void fail(int code, String msg) {

                                    }
                                });
                            }
                        });


                    }

                    @Override
                    public void fail(int code, String msg) {
                    }
                });

            }

            @Override
            public void fail(int code, String msg) {

            }
        });
    }


}
