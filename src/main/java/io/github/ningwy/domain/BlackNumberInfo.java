package io.github.ningwy.domain;

/**
 * 黑名单类
 * Created by ningwy on 2016/4/28.
 */
public class BlackNumberInfo {

    private String number;
    private String mode;

    public BlackNumberInfo() {
    }

    public BlackNumberInfo(String number, String mode) {
        this.number = number;
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "BlackNumberInfo{" +
                "mode='" + mode + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}
