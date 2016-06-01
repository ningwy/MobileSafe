package io.github.ningwy.receivers;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import io.github.ningwy.R;
import io.github.ningwy.activities.OpenAdminActivity;
import io.github.ningwy.services.GPSService;

public class SMSReceiver extends BroadcastReceiver {

    private SharedPreferences sp;
    private DevicePolicyManager dpm;
    private ComponentName componentName;

    public SMSReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(context, MyAdmin.class);
        //1.拿到广播里短信携带的内容
        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        for (Object pdu : pdus) {
            //通过Object创建一个短信对象
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
            //得到电话号码
            String number = sms.getOriginatingAddress();
            //得到安全号码
            String safeNumber = sp.getString("safeNumber", "");
            if (number.contains(safeNumber)) {
                //得到信息内容
                String body = sms.getMessageBody();
                if ("#*location*#".equals(body)) {
                    sendLocation(context,number);
                    abortBroadcast();
                } else if ("#*alarm*#".equals(body)) {
                    playMusic(context);
                    //取消继续广播下去，不让短信指令显示出来
                    abortBroadcast();
                } else if ("#*wipedata*#".equals(body)) {
                    if (dpm.isAdminActive(componentName)) {
                        //恢复出厂设置
//                        dpm.wipeData(0);
                    } else {
                        openAdmin(context);
                    }
                    abortBroadcast();
                } else if ("#*lockscreen*#".equals(body)) {
                    if (dpm.isAdminActive(componentName)) {
                        //锁屏
                        dpm.lockNow();
                    } else {
                        openAdmin(context);
                    }
                    abortBroadcast();
                }
            }
        }
    }

    /**
     * 开启管理员权限
     */
    private void openAdmin(Context context) {
        Intent intent = new Intent(context, OpenAdminActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 发送位置信息
     */
    private void sendLocation(Context context, String number) {
        Intent intent = new Intent(context, GPSService.class);
        context.startService(intent);
        String lastLocation = sp.getString("lastLocation", "");
        //第二个参数为信息中心，如果为null则为默认的信息中心
        //第四个表示如果不为null则当信息发送成功或者失败时广播该第四个参数(PendingIntent)
        //第五个参数类型和第四个一样，都为PendingIntent，表示如果当信息发送到容器中时，这个PendingIntent
        //同样会被广播(至于什么是容器，什么容器，则不清楚了...)
        if (TextUtils.isEmpty(lastLocation)) {
            SmsManager.getDefault().sendTextMessage(number, null, "233333", null, null);
        } else {
            SmsManager.getDefault().sendTextMessage(number, null, lastLocation, null, null);
        }
    }

    /**
     * 播放音乐
     */
    private void playMusic(Context context) {
        MediaPlayer player = MediaPlayer.create(context, R.raw.ylzs);
        player.setVolume(1.0f, 1.0f);
        player.setLooping(true);
        player.start();
    }

}
