package com.novus.navigo.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.novus.navigo.NavigoApplication;
import com.novus.navigo.util.NetworkUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private final String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = NetworkUtil.getConnectivityStatus(context);

        if(status != 0)
            NavigoApplication.internetConnected();
        else
            NavigoApplication.internetDisconnected();
        Log.d(TAG, "" + NavigoApplication.isActivityVisible());
    }
}
