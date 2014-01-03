
package com.mephone.flashbar;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class FloatWindowView extends RelativeLayout implements OnItemClickListener,
        View.OnClickListener {

    private LinearLayout mListView;
    private Context mContext;
    private ArrayList<ApplicationInfo> mAppApps;
    private Button mHandleButton;
    private View mTray;

    private static final int MESSAGE_AUTO_CLOSE_TRAY = 1;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_AUTO_CLOSE_TRAY:
                    mHandleButton.setBackgroundResource(R.drawable.left_handle_closed);
                    mTray.setVisibility(View.GONE);
                    break;
            }
        }
    };

    private class ApplicationInfo {
        String name;
        Drawable icon;
        String uri;
    }

    public FloatWindowView(Context context) {
        this(context, null);
    }

    public FloatWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mListView = (LinearLayout) findViewById(R.id.app_item_list_view);
        loadAllAppsByBatch();
        initListView();
        mHandleButton = (Button) findViewById(R.id.handle);
        mTray = findViewById(R.id.tray);
        mHandleButton.setOnClickListener(this);
        ScrollView scroll = (ScrollView) findViewById(R.id.scroll_view);
        scroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                int action = arg1.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        mHandler.removeMessages(MESSAGE_AUTO_CLOSE_TRAY);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mHandler.sendEmptyMessageDelayed(MESSAGE_AUTO_CLOSE_TRAY, 5 * 1000);
                        break;
                }
                return false;
            }
        });
    }

    private void initListView() {
        mListView.removeAllViews();
        for (int i = 0; i < mAppApps.size(); i++) {
            LayoutInflater mInflater = LayoutInflater.from(mContext);
            View view = mInflater.inflate(R.layout.application, null);
            TextView mApp = (TextView) view.findViewById(R.id.digits);
            ApplicationInfo info = mAppApps.get(i);
            mApp.setText(info.name);
            Drawable drawable = info.icon;
            drawable.setBounds(0, 0, 60, 60);
            mApp.setCompoundDrawables(null, drawable, null, null);
            view.setTag(info.uri);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object obj = v.getTag();
                    Intent intent;
                    try {
                        intent = Intent.parseUri((String) obj, 0);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        mContext.startActivity(intent);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    mHandleButton.setBackgroundResource(R.drawable.left_handle_closed);
                    mTray.setVisibility(View.GONE);
                }
            });
            mListView.addView(view);
        }
    }

    private void loadAllAppsByBatch() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> apps = null;
        mAppApps = new ArrayList<ApplicationInfo>();

        apps = packageManager.queryIntentActivities(mainIntent, PackageManager.PERMISSION_GRANTED);

        if (apps == null) {
            return;
        }
        for (ResolveInfo info : apps) {
            ApplicationInfo aInfo = new ApplicationInfo();
            aInfo.name = info.activityInfo.loadLabel(mContext.getPackageManager()).toString();
            aInfo.icon = info.activityInfo.loadIcon(mContext.getPackageManager());

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.setClassName(info.activityInfo.applicationInfo.packageName,
                    info.activityInfo.name);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            aInfo.uri = intent.toUri(0);
            mAppApps.add(aInfo);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Object obj = arg1.getTag();
        Intent intent;
        try {
            intent = Intent.parseUri((String) obj, 0);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            mContext.startActivity(intent);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        mHandler.removeMessages(MESSAGE_AUTO_CLOSE_TRAY);
        if (mTray.getVisibility() == View.VISIBLE) {
            mHandleButton.setBackgroundResource(R.drawable.left_handle_closed);
            mTray.setVisibility(View.GONE);
        } else {
            mHandleButton.setBackgroundResource(R.drawable.left_handle);
            mTray.setVisibility(View.VISIBLE);
            mHandler.sendEmptyMessageDelayed(MESSAGE_AUTO_CLOSE_TRAY, 5 * 1000);
        }
    }

    public void updateListView() {
        loadAllAppsByBatch();
        initListView();
    }
}
