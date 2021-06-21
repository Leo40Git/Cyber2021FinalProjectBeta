package edu.kfirawad.cyber2021finalprojectbeta.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "C2021FPB:recv:Boot";

    @Override
    public void onReceive(Context ctx, Intent i) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())) {
            Intent si = new Intent(ctx, UserService.class);
            ctx.startService(si);
            Log.i(TAG, "User service auto-started.");
        }
    }
}