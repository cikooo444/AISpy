package com.ai.homework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, HarvesterService.class);
            // Wajib pake ContextCompat.startForegroundService buat Android 8+
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }
}
