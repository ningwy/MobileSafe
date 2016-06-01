package io.github.ningwy.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * 获得服务状态的工具类
 * Created by ningwy on 2016/4/22.
 */
public class ServiceStatusUtils {

    public static boolean isRunningServices(Context context, String className) {
        //1.用ActivityManager去获得后台运行的服务
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //得到100个最新的正在运行的服务，一般100个足够找到所需的服务，因为一般手机最多二三十个服务在运行
        List<ActivityManager.RunningServiceInfo> serviceInfos = am.getRunningServices(100);
        //遍历所有的服务，得到每个服务的全类名，如果与传进来的参数相同，则返回true，没有则返回false
        for (ActivityManager.RunningServiceInfo serviceInfo : serviceInfos) {
            String name = serviceInfo.service.getClassName();
            if (name.equals(className)) {
                return true;
            }
        }
        return false;
    }

}
