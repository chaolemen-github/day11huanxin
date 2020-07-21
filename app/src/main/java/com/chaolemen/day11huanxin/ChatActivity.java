package com.chaolemen.day11huanxin;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chaolemen.day11huanxin.adapter.ChatAdapter;
import com.chaolemen.day11huanxin.utils.AudioUtil;
import com.chaolemen.day11huanxin.utils.ResultCallBack;
import com.chaolemen.day11huanxin.utils.SpUtils;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.recycler_chat)
    RecyclerView recyclerChat;
    @BindView(R.id.et_text)
    EditText etText;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.btn_audio)
    Button btnAudio;
    @BindView(R.id.btn_send1)
    Button btnSend1;
    private EMMessageListener msgListener;
    private List<EMMessage> list;
    private String toName;
    private ChatAdapter chatAdapter;
    private MediaPlayer mMp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        initView();
        initData();
        getOldMsg();
        initGetMessage();
    }

    //获取历史消息
    private void getOldMsg() {
        //获得我和这个好友的聊天回话
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(toName);
        if (conversation == null)//如果没有历史聊天信息，则不用处理
            return;
        //获取此会话的所有消息
        List<EMMessage> messages = conversation.getAllMessages();
        list.addAll(messages);
        chatAdapter.notifyDataSetChanged();
    }


    /**
     * 接收消息
     */
    private void initGetMessage() {

        msgListener = new EMMessageListener() {

            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                //收到消息

                for (int i = 0; i < messages.size(); i++) {
                    EMMessage emMessage = messages.get(i);
                    String from = emMessage.getFrom();//获取类型：用户名
                    if (from.equals(toName)) {
                        list.add(emMessage);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                chatAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }

            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {
                //收到透传消息
            }

            @Override
            public void onMessageRead(List<EMMessage> messages) {
                //收到已读回执
            }

            @Override
            public void onMessageDelivered(List<EMMessage> message) {
                //收到已送达回执
            }

            @Override
            public void onMessageRecalled(List<EMMessage> messages) {
                //消息被撤回
            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {
                //消息状态变动
            }
        };
        //消息监听
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
    }

    private void initView() {

    }

    private void initData() {
        toName = getIntent().getStringExtra("name");
        String name = (String) SpUtils.getParam(this, "name", "");
        tvName.setText(name + "正在和" + toName + "尬聊");

        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, list, name);
        recyclerChat.setAdapter(chatAdapter);

        mMp = new MediaPlayer();
        chatAdapter.setOnItemClickChatListener(new ChatAdapter.OnItemClickChatListener() {
            @Override
            public void onClick(int i) {
                EMMessage emMessage = list.get(i);
                String body = emMessage.getBody().toString();
                if(body.startsWith("voice")){//如果消息以 vocie开头  播放声音
                    String[] strs = body.split(",");//安 逗号 分割，得到每一段信息
                    for (String str:strs) {//循环遍历得到每一段信息
                        if (str.startsWith("localurl")) {//此段信息为  录音的保存路径信息
                            String videoPath = str.split(":")[1];//以 ： 分割的，通过 下标1得到具体的存放路径
                            File file = new File(videoPath);
                            if(file.exists()){//语音文件存在则播放
                                try {
                                    if (mMp != null) {
                                        mMp.stop();
                                        mMp.release();
                                    }
                                    mMp = new MediaPlayer();//创建新的 Mediaplayer对象，播放语音
                                    mMp.setDataSource(videoPath);//加载资源
                                    mMp.prepare();//准备资源
                                    mMp.start();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                Toast.makeText(ChatActivity.this, "录音丢失", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                }
            }
        });
    }



    private void sendText() {
        String trim = etText.getText().toString();
        //创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
        EMMessage message = EMMessage.createTxtSendMessage(trim, toName);
        //发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
        etText.setText("");
        list.add(message);
        chatAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        记得在不需要的时候移除listener，如在activity的onDestroy()时
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
    }


    @OnClick({R.id.btn_send, R.id.btn_audio, R.id.btn_send1})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                sendText();
                break;
            case R.id.btn_audio:
                if(AudioUtil.isRecording){//正在录音，停止停止
                    AudioUtil.stopRecord();
                    btnAudio.setText("开始录音");
                }else {//没有录音
                    startRecord();//开始录音
                    btnAudio.setText("停止录音");
                }
                break;
            case R.id.btn_send1:
                sendAudio();
                break;
        }
    }

    private void sendAudio() {
        if(AudioUtil.isRecording || mFileFath == null){//正在录音或者录音文件为空，不能发送
            return;//返回  下面的代码不再执行
        }
        new Thread(){
            @Override
            public void run() {
                super.run();
                //发送语音信息
                EMMessage message = EMMessage.createVoiceSendMessage(mFileFath, (int) mDuration, toName);
                EMClient.getInstance().chatManager().sendMessage(message);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list.add(message);//把自己发送的消息添加到列表中
                        chatAdapter.notifyDataSetChanged();
                    }
                });
                mFileFath = null;
                mDuration = 0;

            }
        }.start();
    }


    private String mFileFath;
    private long mDuration;
    private void startRecord() {
        AudioUtil.startRecord(new ResultCallBack() {
            @Override
            public void onSuccess(String filePath, long duration) {
                //得到录音的路径和大小
                mFileFath = filePath;
                mDuration = duration;
            }

            @Override
            public void onFail(String str) {

            }
        });
    }
}
