
package com.mephone.flashbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        arg0.startService(new Intent(arg0, FloatWindowService.class));
    }

}
