package io.github.ningwy.domain;

import android.graphics.drawable.Drawable;

/**
 * 应用管理中的应用信息
 * Created by ningwy on 2016/5/13.
 */
public class AppInfo {

    private Drawable icon;
    private String appName;
    private String packageName;
    /**
     * 是否是用户程序
     * true:是
     */
    private boolean isUser;
    /**
     * 是否安装在手机内存上
     * true:是
     */
    private boolean isRom;

    private String appSize;

    public AppInfo( Drawable icon,String appName, String packageName) {
        this.appName = appName;
        this.icon = icon;
        this.packageName = packageName;
    }

    public AppInfo() {
    }

    public String getAppSize() {
        return appSize;
    }

    public void setAppSize(String appSize) {
        this.appSize = appSize;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isRom() {
        return isRom;
    }

    public void setRom(boolean rom) {
        isRom = rom;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

}
