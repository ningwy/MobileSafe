package io.github.ningwy.domain;

import android.graphics.drawable.Drawable;

/**
 * Created by ningwy on 2016/5/18.
 */
public class TaskInfo {

    //应用图标
    private Drawable icon;
    //应用包名
    private String packageName;
    //应用名称
    private String taskName;
    //进程所占的内存
    private long memSize;
    /**
     * 是否为用户进程：
     * true——用户进程
     * false——系统进程
     */
    private boolean isUser;
    /**
     * 是否被勾选：
     * true——勾选
     * false——未勾选
     */
    private boolean isChecked;

    public TaskInfo(Drawable icon, long memSize, String packageName, String taskName) {
        this.icon = icon;
        this.memSize = memSize;
        this.packageName = packageName;
        this.taskName = taskName;
    }

    public TaskInfo() {
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public long getMemSize() {
        return memSize;
    }

    public void setMemSize(long memSize) {
        this.memSize = memSize;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
