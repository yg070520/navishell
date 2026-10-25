package com.example.navishell;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    private static final String TAG = "NaviShell";
    
    // TODO: 替换为你需要测试拉起的真正目标 App 包名
    private static final String TARGET_APP_PACKAGE = "com.example.cartracker";
    // 目标高德车机版包名
    private static final String AMAP_AUTO_PACKAGE = "com.autonavi.amapauto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "壳 App 被车机拉起，准备启动目标程序与高德地图...");

        // 1. 启动你的测试程序
        launchApp(TARGET_APP_PACKAGE, "目标测试程序");

        // 2. 启动真正的高德地图车机版
        launchApp(AMAP_AUTO_PACKAGE, "高德地图车机版");

        // 3. 任务完成，透明 Activity 自动销毁
        finish();
    }

    private void launchApp(String packageName, String appName) {
        try {
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(packageName);

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                              | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity(intent);
                Log.d(TAG, "成功拉起: " + appName);
            } else {
                Log.e(TAG, "未找到应用: " + appName);
            }
        } catch (Exception e) {
            Log.e(TAG, "启动 " + appName + " 失败: " + e.getMessage());
        }
    }
}
