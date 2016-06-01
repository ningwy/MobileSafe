package io.github.ningwy.domain;

/**
 * 手机杀毒功能中扫描应用的信息
 * Created by ningwy on 2016/5/27.
 */
public class ScanInfo {

    /**
     * 应用名字
     */
    private String name;
    /**
     * 应用包名
     */
    private String packageName;
    /**
     * 是否为病毒
     * true:是病毒
     * false:不是病毒
     */
    private boolean isVirus;

    public ScanInfo() {
    }

    public ScanInfo(boolean isVirus, String name, String packageName) {
        this.isVirus = isVirus;
        this.name = name;
        this.packageName = packageName;
    }

    public boolean isVirus() {
        return isVirus;
    }

    public void setVirus(boolean virus) {
        isVirus = virus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
