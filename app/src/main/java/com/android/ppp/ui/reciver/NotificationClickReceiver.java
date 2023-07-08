package com.android.ppp.ui.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.ppp.ui.login.LoginActivity;

public class NotificationClickReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent newIntent = new Intent(context, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(newIntent);
    }
}