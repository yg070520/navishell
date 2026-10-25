package com.example.navishell;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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

        List<Intent> launchIntents = new ArrayList<>();
        List<String> missingApps = new ArrayList<>();

        addLaunchIntent(launchIntents, missingApps,
                new String[]{TARGET_APP_PACKAGE}, "目标测试程序");
        addLaunchIntent(launchIntents, missingApps,
                new String[]{AMAP_AUTO_PACKAGE, AMAP_PHONE_PACKAGE}, "高德地图");

        if (!missingApps.isEmpty()) {
            String message = "未找到应用: " + TextUtils.join(", ", missingApps);
            Log.e(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        if (launchIntents.isEmpty()) {
            finish();
            return;
        }

        try {
            // 一次性提交两个启动任务，避免第二个启动因壳 App 已退到后台而被拦截。
            startActivities(launchIntents.toArray(new Intent[0]));
            Log.d(TAG, "已提交 " + launchIntents.size() + " 个启动任务");
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.e(TAG, "启动失败", e);
            Toast.makeText(this, "启动失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        finish();
    }

    private void addLaunchIntent(List<Intent> launchIntents, List<String> missingApps,
                                 String[] packageNames, String appName) {
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
                    launchIntents.add(intent);
                    Log.d(TAG, "准备启动: " + appName + " (" + packageName + ")");
                    return;
                }
            }

            missingApps.add(appName + " (" + TextUtils.join(" / ", packageNames) + ")");
        } catch (SecurityException e) {
            Log.e(TAG, "没有权限查询或启动 " + appName, e);
            missingApps.add(appName + " (" + TextUtils.join(" / ", packageNames) + ")");
        }
    }
}
