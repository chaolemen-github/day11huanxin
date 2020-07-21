package com.chaolemen.day11huanxin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chaolemen.day11huanxin.utils.SpUtils;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.et_user)
    EditText etUser;
    @BindView(R.id.et_pass)
    EditText etPass;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.btn_regist)
    Button btnRegist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //登录必须要走的代码
        EMClient.getInstance().groupManager().loadAllGroups();
        EMClient.getInstance().chatManager().loadAllConversations();
        //判断是否登录，是否设置为自动登录
        boolean loggedInBefore = EMClient.getInstance().isLoggedInBefore();
        if (loggedInBefore){
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            finish();
            return;
        }


        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_login, R.id.btn_regist})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                login();
                break;
            case R.id.btn_regist:
                //跳转到注册页面
                Intent intent = new Intent(LoginActivity.this, RegistActivity.class);
                startActivityForResult(intent,200);
                break;
        }
    }

    private void login() {
        String user = etUser.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        //判断账号密码是否为空
        if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)) {
            //退出登录
            EMClient.getInstance().login(user, pass, new EMCallBack() {
                @Override
                public void onSuccess() {
                    //登录成功需回调
                    EMClient.getInstance().groupManager().loadAllGroups();
                    EMClient.getInstance().chatManager().loadAllConversations();
                    Log.e("main", "登录聊天服务器成功！");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //土司
                            Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                        }
                    });

                    //记录登录状态
                    SpUtils.setParam(LoginActivity.this,"name",user);
                    //跳转
                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    finish();//关闭登录页面
                }

                @Override
                public void onError(int i, String s) {
                    //登陆失败回调的方法
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "登陆失败"+s, Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                @Override
                public void onProgress(int i, String s) {

                }
            });
        } else {
            Toast.makeText(this, "账号密码不能为空", Toast.LENGTH_SHORT).show();
        }

    }



    //回传值
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==200&&resultCode==300){
            String user = data.getStringExtra("user");
            etUser.setText(user);
        }
    }
}
