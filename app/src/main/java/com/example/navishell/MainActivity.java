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
    
    // TODO: 替换为你需要测试拉起的真正目标 App 包名
    private static final String TARGET_APP_PACKAGE = "com.example.cartracker";
    // 目标高德车机版包名
    private static final String AMAP_AUTO_PACKAGE = "com.autonavi.amapauto";
    // 手机测试时可回退到高德手机版
    private static final String AMAP_PHONE_PACKAGE = "com.autonavi.minimap";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "壳 App 被车机拉起，准备启动目标程序与高德地图...");

        boolean trackerStarted = launchApp(TARGET_APP_PACKAGE, "目标测试程序");

        // 先让第一个应用完成切前台，再启动第二个；不同应用不能在一次 startActivities 中提交。
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean amapStarted = launchApp(
                    new String[]{AMAP_AUTO_PACKAGE, AMAP_PHONE_PACKAGE}, "高德地图");
            if (!trackerStarted && !amapStarted) {
                Toast.makeText(this, "未找到目标应用", Toast.LENGTH_LONG).show();
            }
            finish();
        }, 1200);
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
