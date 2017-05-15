package plu.red.reversi.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import plu.red.reversi.core.util.ChatMessage;

/**
 * Created by daniel on 5/13/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class ChatListAdapter extends ArrayAdapter<ChatMessage> implements ServiceConnection {

    String mChannelName;

    GameService.LocalBinder mServiceConnection;

    /**
     * Constructor
     *
     * @param context  The current context.
     */
    public ChatListAdapter(@NonNull Context context, String channel) {
        super(context, R.layout.fragment_list_chat);

        this.mChannelName = channel;

        // use the context to get in contact with the chat accumulator in the service
        context.bindService(new Intent(context, GameService.class), this, 0);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // good
        notifyDataSetChanged();
        mServiceConnection = (GameService.LocalBinder)service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        // may be bad
        notifyDataSetChanged();
        mServiceConnection = null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = new ViewHolder().getView();

        ViewHolder holder = (ViewHolder)convertView.getTag();

        holder.apply(mServiceConnection.getChannelMessage(mChannelName, position));

        return convertView;
    }

    public class ViewHolder {
        private View mView;

        private TextView mUserText;
        private TextView mMessageText;

        public ViewHolder() {
            LayoutInflater inflater = ((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            mView = inflater.inflate(R.layout.fragment_list_chat, null);
            mView.setTag(this);

            mUserText = (TextView)mView.findViewById(R.id.chat_user);
            mMessageText = (TextView)mView.findViewById(R.id.chat_message);
        }

        public void apply(ChatMessage msg) {
            mUserText.setTextColor(msg.usercolor.composite);
            mUserText.setText(msg.username);
            mMessageText.setText(msg.message);
        }

        public View getView() {
            return mView;
        }
    }
}
