package com.example.mdp.Chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mdp.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter{
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context mContext;
    private List<Message> mMessageList;

    public ChatAdapter(Context context, List<Message> messageList) {
        mContext = context;
        mMessageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = mMessageList.get(position);
        return msg.getSender();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatleft, parent, false);
            return new ReceivedHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatright, parent, false);
            return new SentHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = (Message) mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentHolder) holder).bind(message, mContext);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedHolder) holder).bind(message, mContext);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


}
