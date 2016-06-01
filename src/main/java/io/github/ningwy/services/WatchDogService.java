package io.github.ningwy.services;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.github.ningwy.activities.EnterPwdActivity;
import io.github.ningwy.db.dao.AppLockDao;

public class WatchDogService extends Service {

    /**
     * 用来让当WatchDogService被stop时停止循环遍历任务栈的topActivity
     * true:循环遍历
     * false:停止遍历
     */
    private boolean flag = false;

    private AppLockDao dao;

    /**
     * 在程序锁数据库中的包名集合
     */
    private List<String> packageNameList = new ArrayList<>();

    /**
     * 任务栈topActivity的包名
     */
    private String packageName = "";

    /**
     * 已经启动的应用的包名，用于停止重新对同一个应用启动多次的输入密码界面
     */
    private String stopProtectionPackageName;

    private StopProtectionReceiver receiver;

    private DBChangedObserver observer;

    public WatchDogService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        dao = new AppLockDao(this);
        packageNameList = dao.queryAllLockedApp();

        //注册广播——用于监听当应用输入密码启动时，停止弹出输入密码页面
        receiver = new StopProtectionReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("io.github.ningwy.stopprotection");
        registerReceiver(receiver, filter);

        //分线程中循环遍历
        new Thread() {
            @Override
            public void run() {
                flag = true;
                while (flag) {
                    //获取包名
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { //For versions less than lollipop
                        ActivityManager am = ((ActivityManager) getSystemService(ACTIVITY_SERVICE));
                        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(5);
                        packageName = taskInfo.get(0).topActivity.getPackageName();
                    } else { //For versions Lollipop and above
//                        List<AndroidAppProcess> processes = ProcessManager.getRunningForegroundApps(getApplicationContext());
//                        Collections.sort(processes, new ProcessManager.ProcessComparator());
//                        for (int i = 0; i <= processes.size() - 1; i++) {
//                            if ((i + 1) <= processes.size() - 1) { //If processes.get(i+1) available, then that app is the top app
//                                packageName = processes.get(i + 1).name;
//                            } else if (i != 0) { //If the last package name is "com.google.android.gms" then the package name above this is the top app
//                                packageName = processes.get(i - 1).name;
//                            } else {
//                                if (i == processes.size() - 1) { //If only one package name available
//                                    packageName = processes.get(i).name;
//                                }
//                            }
//                        }
                        if (needPermissionForBlocking(getApplicationContext())) {
                            //如果用户没有授权，引导用户去设置页面授权
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            //在service中开启activity需要为intent添加FLAG_ACTIVITY_NEW_TASK的flag
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            packageName = getTopPackage();
                        }
                        Log.e("TAG", "top app = " + packageName);
                    }
                    if (packageNameList.contains(packageName)) {
                        if (packageName.equals(stopProtectionPackageName)) {//说明该应用已经启动了，停止
                            //什么都不做
                        } else {//否则启动输入密码页面
                            Intent intent = new Intent(WatchDogService.this, EnterPwdActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("packageName", packageName);
                            startActivity(intent);
                        }
                    }
                    SystemClock.sleep(50);
                }
            }
        }.start();

        observer = new DBChangedObserver(new Handler());
        Uri uri = Uri.parse("content://io.github.ningwy.dbchange");
        getContentResolver().registerContentObserver(uri, true, observer);
    }

    /**
     * 获得top activity的包名
     * @return
     */
    @TargetApi(21)
    public String getTopPackage(){
        long ts = System.currentTimeMillis();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts-1000, ts);
        if (usageStats == null || usageStats.size() == 0) {//如果为空则返回""
            return "";
        }
        Collections.sort(usageStats, new RecentUseComparator());
        return usageStats.get(0).getPackageName();
    }

    /**
     *
     */
    @TargetApi(21)
    static class RecentUseComparator implements Comparator<UsageStats> {

        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
        }
    }

    /**
     * 使用UsageStatsManager需要用户允许开启，该方法用于判断用户是否已经授权
     * @param context
     * @return true:还没有授权 false:已经授权
     */
    @TargetApi(19)
    public static boolean needPermissionForBlocking(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return  (mode != AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

    @Override
    public void onDestroy() {
        flag = false;

        //取消注册
        unregisterReceiver(receiver);
        receiver = null;

        getContentResolver().unregisterContentObserver(observer);
    }

    private class StopProtectionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            stopProtectionPackageName = intent.getStringExtra("packageName");
        }
    }

    private class DBChangedObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public DBChangedObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            packageNameList = dao.queryAllLockedApp();
        }
    }
}
