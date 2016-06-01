package io.github.ningwy.engine;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

import io.github.ningwy.domain.AppInfo;

/**
 * AppInfo的一个引擎
 * Created by ningwy on 2016/5/15.
 */
public class AppInfoProdiver {

    /**
     * 获得所有的应用组成的列表
     * @param context 上下文
     * @return 得到的应用列表
     */
    public static List<AppInfo> getAllAppInfo(Context context) {
        List<AppInfo> appInfos = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
        for (PackageInfo info : packageInfos) {
            //应用图标
            Drawable icon = info.applicationInfo.loadIcon(pm);
            //应用名称
            String label = info.applicationInfo.loadLabel(pm).toString();
            //应用包名
            String packageName = info.packageName;
            //应用大小
            AppInfo appInfo = new AppInfo(icon, label, packageName);
            //得到应用程序的标识，通过此标识作一些运算可以得知应用程序更详细的信息，
            //例如安装在哪个区域，是系统应用还是用户应用
            int flags = info.applicationInfo.flags;
            //通过FLAG_SYSTEM与flags与运算来判断应用是否是系统应用
            if ((flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                //用户程序
                appInfo.setUser(true);
            } else {
                //系统应用
                appInfo.setUser(false);
            }
            //判断应用是内部存储还是外部存储
            if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0) {
                //内部存储
                appInfo.setRom(true);
            } else {
                //外部存储
                appInfo.setRom(false);
            }
            appInfos.add(appInfo);
        }
        return appInfos;
    }

    /**
     * 寻找应用的启动页面的Activity
     * @param packageManager 包管理器
     * @param packageName 包名
     * @return ResolveInfo
     */
    public static ResolveInfo findActivitiesForPackage(PackageManager packageManager, String packageName) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        boolean isfinded = false;
        //通过CATEGORY_DEFAULT参数查找应用程序默认的Activity
        //为什么不是CATEGORY_MAIN参数，因为有些应用没有main activity，从而找不到入口
        mainIntent.addCategory(Intent.CATEGORY_DEFAULT);
        List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        ResolveInfo info = null;
        if (apps != null) {
            // Find all activities that match the packageName
            int count = apps.size();
            for (int i = 0; i < count; i++) {
                info = apps.get(i);
                ActivityInfo activityInfo = info.activityInfo;
                if (packageName.equals(activityInfo.packageName)) {
                    isfinded = true;
                    break;//只要一个
                }
            }
        }
        if(!isfinded){
            return null;
        }
        return info;
    }

}
