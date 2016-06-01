package io.github.ningwy.services;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Timer;
import java.util.TimerTask;

import io.github.ningwy.R;
import io.github.ningwy.receivers.MyAppWidgetReceiver;
import io.github.ningwy.utils.SystemInfoUtils;

public class UpdateWidgetService extends Service {

    private AppWidgetManager awm;
    private Timer timer;
    private TimerTask task;
    private ScreenReceiver receiver;

    public UpdateWidgetService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        awm = AppWidgetManager.getInstance(this);
        receiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(receiver, filter);
        startUpdate();

    }

    private void startUpdate() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                Log.e("TAG", "还在更新");

                ComponentName provider = new ComponentName(UpdateWidgetService.this, MyAppWidgetReceiver.class);
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.example_keyguard);
                int count = SystemInfoUtils.getRunningProcessCount(UpdateWidgetService.this);
                String availRam = Formatter.formatFileSize(UpdateWidgetService.this, SystemInfoUtils.getAvailRam(UpdateWidgetService.this));
                views.setTextViewText(R.id.process_count, "正在运行软件:" + count);
                views.setTextViewText(R.id.process_memory, "可用内存:" + availRam);
                /**
                 * 设置点击事件——清理进程
                 */
                //Intent定义了点击事件要做什么
                Intent intent = new Intent();
                intent.setAction("io.github.ningwy.killprocess");
                //PendingIntent是一个延迟意图，具体作用有待查究。因为：为什么不直接把intent以第二个参数的身份直接塞入
                //views.setOnClickPendingIntent(R.id.btn_clear, pendingIntent)方法中，而是要外包一层PendingIntent
                PendingIntent pendingIntent = PendingIntent.getBroadcast(UpdateWidgetService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.btn_clear, pendingIntent);
                awm.updateAppWidget(provider, views);
            }
        };
        timer.schedule(task, 500, 4000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null && task != null) {
            timer.cancel();
            task.cancel();
            timer = null;
            task = null;
        }
    }

    private class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == Intent.ACTION_SCREEN_OFF) {
                //锁屏
                if (timer != null && task != null) {
                    timer.cancel();
                    task.cancel();
                    timer = null;
                    task = null;
                }
            } else if (intent.getAction() == Intent.ACTION_SCREEN_ON){
                //打开屏幕
                if (timer == null && task == null) {
                    startUpdate();
                }
            }
        }
    }
}
