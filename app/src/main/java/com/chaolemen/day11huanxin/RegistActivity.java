package com.chaolemen.day11huanxin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegistActivity extends AppCompatActivity {

    @BindView(R.id.et_user)
    EditText etUser;
    @BindView(R.id.et_pass)
    EditText etPass;
    @BindView(R.id.btn_regist)
    Button btnRegist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_regist)
    public void onViewClicked() {
        register();
    }

    private void register() {
        String user = etUser.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        //判断账号密码是否为空
        if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //调用环信的注册代码
                        EMClient.getInstance().createAccount(user, pass);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //吐司成功
                                Toast.makeText(RegistActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                //将账号回传给登录页面
                                Intent intent = getIntent();
                                intent.putExtra("user", user);
                                setResult(300, intent);
                                finish();//关闭注册页面
                            }
                        });
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //注册失败吐司
                                Toast.makeText(RegistActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).start();
        } else {
            Toast.makeText(this, "账号密码不能为空", Toast.LENGTH_SHORT).show();
        }
    }
}
