package io.github.ningwy.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.github.ningwy.domain.TaskInfo;
import io.github.ningwy.engine.TaskInfoProvider;

public class KillProcessService extends Service {

    private ScreenOffReceiver receiver;
    private Timer timer;
    private TimerTask timerTask;

    public KillProcessService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.e("TAG", "清理完成");
            }
        };
        timer.schedule(timerTask, 500, 4000);

        receiver = new ScreenOffReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        receiver = null;

        if (timer != null && timerTask != null) {
            timer.cancel();
            timerTask.cancel();
            timer = null;
            timerTask = null;
        }
    }

    private class ScreenOffReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ActivityManager am = (ActivityManager) KillProcessService.this.getSystemService(Context.ACTIVITY_SERVICE);
            List<TaskInfo> taskInfos = TaskInfoProvider.getAllTask(KillProcessService.this);
            for (TaskInfo taskInfo : taskInfos) {
                am.killBackgroundProcesses(taskInfo.getPackageName());
            }
        }
    }
}
