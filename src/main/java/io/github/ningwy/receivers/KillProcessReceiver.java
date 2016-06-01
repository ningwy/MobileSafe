package io.github.ningwy.receivers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import io.github.ningwy.domain.TaskInfo;
import io.github.ningwy.engine.TaskInfoProvider;

public class KillProcessReceiver extends BroadcastReceiver {
    public KillProcessReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<TaskInfo> taskInfos = TaskInfoProvider.getAllTask(context);
        for (TaskInfo taskInfo : taskInfos) {
            am.killBackgroundProcesses(taskInfo.getPackageName());
        }
    }

}
