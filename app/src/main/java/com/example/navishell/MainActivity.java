package com.example.navishell;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String TAG = "NaviShell";
    
    // CarTracker 通过后台广播启动，不打开主界面
    private static final String TARGET_APP_PACKAGE = "com.example.cartracker";
    private static final String TARGET_APP_RECEIVER =
            "com.example.cartracker.TriggerReceiver";
    private static final String ACC_ON_ACTION = "com.android.action.ACC_ON";
    // 目标高德车机版包名
    private static final String AMAP_AUTO_PACKAGE = "com.autonavi.amapauto";
    // 手机测试时可回退到高德手机版
    private static final String AMAP_PHONE_PACKAGE = "com.autonavi.minimap";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "壳 App 被车机拉起，准备启动目标程序与高德地图...");

        boolean trackerStarted = startBackgroundReceiver(
                TARGET_APP_PACKAGE, TARGET_APP_RECEIVER, ACC_ON_ACTION);

        // 给 CarTracker 留出后台初始化时间，再启动高德并让它保持前台。
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean amapStarted = launchApp(
                    new String[]{AMAP_AUTO_PACKAGE, AMAP_PHONE_PACKAGE}, "高德地图");
            if (!trackerStarted && !amapStarted) {
                Toast.makeText(this, "未找到目标应用", Toast.LENGTH_LONG).show();
            }
            finish();
        }, 500);
    }

    private boolean startBackgroundReceiver(String packageName, String receiverName,
                                            String action) {
        try {
            Intent intent = new Intent(action)
                    .setClassName(packageName, receiverName)
                    .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendBroadcast(intent);
            Log.d(TAG, "已发送后台启动广播: " + action + " -> " + receiverName);
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "后台启动广播失败", e);
            return false;
        }
    }

    private boolean launchApp(String packageName, String appName) {
        return launchApp(new String[]{packageName}, appName);
    }

    private boolean launchApp(String[] packageNames, String appName) {
        try {
            PackageManager pm = getPackageManager();
            for (String packageName : packageNames) {
                Intent intent = pm.getLaunchIntentForPackage(packageName);

                if (intent == null) {
                    Intent fallback = new Intent(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_LAUNCHER)
                            .setPackage(packageName);
                    if (pm.resolveActivity(fallback, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                        intent = fallback;
                    }
                }

                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    startActivity(intent);
                    Log.d(TAG, "已启动: " + appName + " (" + packageName + ")");
                    return true;
                }
            }

            Log.e(TAG, "未找到应用: " + appName
                    + " (" + TextUtils.join(" / ", packageNames) + ")");
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.e(TAG, "启动 " + appName + " 失败", e);
        }
        return false;
    }
}
