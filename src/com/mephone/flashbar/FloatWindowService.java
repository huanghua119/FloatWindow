
package com.mephone.flashbar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class FloatWindowService extends Service {

    protected WindowManager mWindowManager;
    private FloatWindowView mRootView;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null
                    && (Intent.ACTION_INSTALL_PACKAGE.equals(action)
                            || Intent.ACTION_UNINSTALL_PACKAGE.equals(action)
                            || Intent.ACTION_PACKAGE_ADDED.equals(action)
                            || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                            || Intent.ACTION_PACKAGE_REPLACED.equals(action))) {
                mRootView.updateListView();
                WindowManager.LayoutParams wmParams = ((MyApplication) getApplication())
                        .getMywmParams();
                mWindowManager.updateViewLayout(mRootView, wmParams);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_INSTALL_PACKAGE);
        intentFilter.addAction(Intent.ACTION_UNINSTALL_PACKAGE);
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        registerReceiver(mReceiver, intentFilter);
        Log.i("huanghua", "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRootView != null) {
            mWindowManager.removeView(mRootView);
        }
        unregisterReceiver(mReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        WindowManager.LayoutParams wmParams = ((MyApplication) getApplication())
                .getMywmParams();
        LinearLayout tmpRoot = new LinearLayout(this);
        if (mRootView != null) {
            mWindowManager.removeView(mRootView);
        }
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.width = LayoutParams.WRAP_CONTENT;
        wmParams.height = LayoutParams.WRAP_CONTENT;
        wmParams.gravity = Gravity.LEFT;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.setTitle("floatView");
        mRootView = (FloatWindowView) LayoutInflater.from(this).inflate(
                R.layout.activity_main, tmpRoot, false);
        mWindowManager.addView(mRootView, wmParams);
        mRootView.setVisibility(View.VISIBLE);
        mRootView.invalidate();
        mWindowManager.updateViewLayout(mRootView, wmParams);
        return super.onStartCommand(intent, flags, startId);
    }
}
