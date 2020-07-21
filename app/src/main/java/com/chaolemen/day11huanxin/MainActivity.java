package com.chaolemen.day11huanxin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaolemen.day11huanxin.adapter.FriendAdapter;
import com.chaolemen.day11huanxin.utils.SpUtils;
import com.hyphenate.chat.EMClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_main)
    RecyclerView recyclerMain;
    private List<String> list;
    private FriendAdapter friendAdapter;
    private String name;
    private boolean isPermissionRequested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        initData();
        requestPermission();//申请动态权限
    }

    /**
     * Android6.0之后需要动态申请权限
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionRequested) {

            isPermissionRequested = true;

            ArrayList<String> permissionsList = new ArrayList<>();

            String[] permissions = {
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_PHONE_STATE
            };
            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    permissionsList.add(perm);
                    // 进入到这里代表没有权限.
                }
            }

            if (permissionsList.isEmpty()) {
                return;
            } else {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 0);
            }
        }
    }

    private void initView() {
        name = (String) SpUtils.getParam(this, "name", "未登录");
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);

        TextView textView = (TextView) toolbar.getChildAt(0);
        textView.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
        textView.setGravity(Gravity.CENTER);

        recyclerMain.setLayoutManager(new LinearLayoutManager(this));
        recyclerMain.addItemDecoration(new DividerItemDecoration(this, RecyclerView.VERTICAL));

        list = new ArrayList<>();
        list.add("aaa");
        list.add("bbb");
        list.add("ccc");

        friendAdapter = new FriendAdapter(this, list);
        recyclerMain.setAdapter(friendAdapter);

    }

    private void initData() {
        friendAdapter.setOnClickItemFriendListener(new FriendAdapter.OnClickItemFriendListener() {
            @Override
            public void onClick(int i) {
                String toName = list.get(i);
                if (name.equals(toName)){
                    Toast.makeText(MainActivity.this, "不能和自己聊", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("name",toName);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * 退出登录
     *
     * @param menu
     * @return
     */
    //选项菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "退出登录");
        return super.onCreateOptionsMenu(menu);
    }

    //退出登录
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            EMClient.getInstance().logout(true);
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
