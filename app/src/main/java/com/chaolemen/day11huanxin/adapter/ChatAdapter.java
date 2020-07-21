package com.chaolemen.day11huanxin.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chaolemen.day11huanxin.R;
import com.hyphenate.chat.EMMessage;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int type_0 = 0;
    private static final int type_1 = 1;

    Context context;
    List<EMMessage> list;
    String name;

    public ChatAdapter(Context context, List<EMMessage> list, String name) {
        this.context = context;
        this.list = list;
        this.name = name;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == type_0) {
            View inflate = LayoutInflater.from(context).inflate(R.layout.activity_chat_item0, viewGroup, false);
            return new ViewHolder0(inflate);
        } else {
            View inflate1 = LayoutInflater.from(context).inflate(R.layout.activity_chat_item1, viewGroup, false);
            return new ViewHolder1(inflate1);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        int itemViewType = getItemViewType(i);

        EMMessage emMessage = list.get(i);
        String s = emMessage.getBody().toString();
        String from = emMessage.getFrom();
        if (itemViewType == type_0) {
            ViewHolder0 viewHolder0 = (ViewHolder0) viewHolder;
            viewHolder0.tvUser.setText(from);
            viewHolder0.tvText.setText(s);
        } else {

            ViewHolder1 viewHolder1 = (ViewHolder1) viewHolder;
            viewHolder1.tvUser1.setText(from);
            viewHolder1.tvText1.setText(s);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickChatListener!=null){
                    onItemClickChatListener.onClick(i);
                }
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        EMMessage emMessage = list.get(position);
        String from = emMessage.getFrom();
        if (from.equals(name)) {
            return type_1;
        } else {
            return type_0;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder0 extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_user)
        TextView tvUser;
        @BindView(R.id.tv_text)
        TextView tvText;

        public ViewHolder0(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class ViewHolder1 extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_user1)
        TextView tvUser1;
        @BindView(R.id.tv_text1)
        TextView tvText1;

        public ViewHolder1(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    OnItemClickChatListener onItemClickChatListener;

    public void setOnItemClickChatListener(OnItemClickChatListener onItemClickChatListener) {
        this.onItemClickChatListener = onItemClickChatListener;
    }

    public interface OnItemClickChatListener{
        void onClick(int i);
    }


}
