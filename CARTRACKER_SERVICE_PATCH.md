# CarTracker 前台服务接入

NaviShell 通过显式组件启动：

- 包名：`com.example.cartracker`
- Service：`com.example.cartracker.TrackerService`
- Action：`com.example.cartracker.action.START_FROM_NAVISHELL`

## AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<application>
    <service
        android:name=".TrackerService"
        android:exported="true"
        android:foregroundServiceType="location" />
</application>
```

Android 14+ 如果服务需要定位，还应声明：

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
```

## TrackerService.kt

```kotlin
package com.example.cartracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

class TrackerService : Service() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("CarTracker")
                .setContentText("车辆跟踪服务运行中")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("CarTracker")
                .setContentText("车辆跟踪服务运行中")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
        }

        startForeground(NOTIFICATION_ID, notification)

        // 将现有 CarTracker 后台逻辑迁移或绑定到这里。
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "CarTracker 后台服务",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    companion object {
        private const val CHANNEL_ID = "cartracker_service"
        private const val NOTIFICATION_ID = 1001
    }
}
```

注意：`R.drawable.ic_launcher_foreground` 需要替换成 CarTracker 项目里实际存在的小图标资源。
