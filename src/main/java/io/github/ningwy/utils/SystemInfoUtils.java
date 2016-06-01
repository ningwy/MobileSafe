package io.github.ningwy.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import com.jaredrummler.android.processes.ProcessManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * 获取系统信息的工具类
 * Created by ningwy on 2016/5/17.
 */
public class SystemInfoUtils {

    /**
     * 获取当前运行的进程数量
     * @param context 上下文
     * @return 当前进程的数量
     */
    public static int getRunningProcessCount(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= 21) {
            return ProcessManager.getRunningAppProcesses().size();
        } else {
            return am.getRunningAppProcesses().size();
        }
    }

    /**
     * 获得可用ram
     * @param context 上下文
     * @return
     */
    public static long getAvailRam(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(outInfo);
        return outInfo.availMem;
    }

    /**
     * 获得总的ram
     * @param context 上下文
     * @return
     */
    public static long getTotalRam(Context context) {
//        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
//        am.getMemoryInfo(outInfo);
//        return outInfo.totalMem;
        //上述方法只适合于API16及以上的版本，在低版本的系统中会奔溃，所以另采用方法
        //其实安卓系统地内存和CPU信息存储于 /proc/meminfo 和/proc/cpuinfo两个文件下，
        //因此可通过读取这两个文件来获得内存信息
        File file = new File("/proc/meminfo");
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            //MemTotal:        1020804 kB    --进入到系统里面看到读取到的是这样的数据
            String totalMemory = br.readLine();
            StringBuffer buffer = new StringBuffer();
            for (char c : totalMemory.toCharArray()) {
                //遍历得到的第一行数据，把其中的数字取出来
                if (c >= '0' && c <= '9') {
                    buffer.append(c);
                }
            }
            return Long.valueOf(buffer.toString()) * 1024;//需要转换为byte
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
