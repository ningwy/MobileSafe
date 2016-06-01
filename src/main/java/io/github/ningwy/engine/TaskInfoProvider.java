package io.github.ningwy.engine;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Debug;
import android.util.Log;

import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.util.ArrayList;
import java.util.List;

import io.github.ningwy.R;
import io.github.ningwy.domain.TaskInfo;

/**
 * 任务进程信息提供引擎
 * Created by ningwy on 2016/5/18.
 */
public class TaskInfoProvider {

    /**
     * 获得所有进程信息
     * @param context
     * @return
     */
    public static List<TaskInfo> getAllTask(Context context) {
        if (Build.VERSION.SDK_INT >= 21) {
           return getAllTaskInNewVersion(context);
        } else {
            return getAllTaskInOldVersion(context);
        }
    }

    /**
     * 获得所有进程信息
     * Android 5.0以上版本用这个方法
     *
     * @param context 上下文
     * @return 返回进程信息类TaskInfo组成的List
     */
    public static List<TaskInfo> getAllTaskInNewVersion(Context context) {
        List<TaskInfo> taskInfos = new ArrayList<>();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager pm = context.getPackageManager();
        List<AndroidAppProcess> processInfos = ProcessManager.getRunningAppProcesses();
        for (AndroidAppProcess processInfo : processInfos) {
            String packageName = processInfo.name;
            TaskInfo taskInfo = new TaskInfo();
            //包名--也即进程名称
            taskInfo.setPackageName(packageName);
            try {
                PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
                //应用图标
                Drawable icon = packageInfo.applicationInfo.loadIcon(pm);
                taskInfo.setIcon(icon);
                //应用名称
                String taskName = packageInfo.applicationInfo.loadLabel(pm).toString();
                taskInfo.setTaskName(taskName);
                //根据进程id来获得内存信息
                Debug.MemoryInfo memInfo = am.getProcessMemoryInfo(new int[]{processInfo.pid})[0];
                //getTotalPrivateDirty方法返回的是kb，所以乘以1024变换为byte
                int memSize = memInfo.getTotalPrivateDirty() * 1024;
                taskInfo.setMemSize(memSize);
                int flag = packageInfo.applicationInfo.flags;
                if ((flag & packageInfo.applicationInfo.FLAG_SYSTEM) == 0) {
                    //用户进程
                    taskInfo.setUser(true);
                } else {
                    //系统进程
                    taskInfo.setUser(false);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                taskInfo.setIcon(context.getResources().getDrawable(R.drawable.app));
                taskInfo.setTaskName(packageName);
            }
            if (taskInfo != null) {
                taskInfos.add(taskInfo);
            }
        }

        return taskInfos;
    }

    /**
     * 获得所有进程信息
     * Android 5.0以下版本用这个方法
     *
     * @param context 上下文
     * @return 返回进程信息类TaskInfo组成的List
     */
    public static List<TaskInfo> getAllTaskInOldVersion(Context context) {
        List<TaskInfo> taskInfos = new ArrayList<>();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager pm = context.getPackageManager();
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
            String packageName = processInfo.processName;
            TaskInfo taskInfo = new TaskInfo();
            //包名--也即进程名称
            taskInfo.setPackageName(packageName);
            try {
                PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
                //应用图标
                Drawable icon = packageInfo.applicationInfo.loadIcon(pm);
                taskInfo.setIcon(icon);
                //应用名称
                String taskName = packageInfo.applicationInfo.loadLabel(pm).toString();
                taskInfo.setTaskName(taskName);
                //根据进程id来获得内存信息
                Debug.MemoryInfo memInfo = am.getProcessMemoryInfo(new int[]{processInfo.pid})[0];
                //getTotalPrivateDirty方法返回的是kb，所以乘以1024变换为byte
                int memSize = memInfo.getTotalPrivateDirty() * 1024;
                taskInfo.setMemSize(memSize);
                int flag = packageInfo.applicationInfo.flags;
                if ((flag & packageInfo.applicationInfo.FLAG_SYSTEM) == 0) {
                    //用户进程
                    taskInfo.setUser(true);
                } else {
                    //系统进程
                    taskInfo.setUser(false);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                taskInfo.setIcon(context.getResources().getDrawable(R.drawable.app));
                taskInfo.setTaskName(packageName);
            }
            taskInfos.add(taskInfo);
        }

        return taskInfos;
    }

}
