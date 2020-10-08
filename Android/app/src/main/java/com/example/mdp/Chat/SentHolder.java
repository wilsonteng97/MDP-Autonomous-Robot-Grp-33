package com.example.mdp.Chat;

import android.content.Context;
import android.icu.util.DateInterval;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mdp.R;

public class SentHolder extends RecyclerView.ViewHolder {
    TextView msg;

    public SentHolder (View view) {
        super(view);
        msg = view.findViewById(R.id.msgBody);
    }

    public void bind(Message theMsg, Context c) {
        msg.setText(theMsg.getMessage());
    }

}
