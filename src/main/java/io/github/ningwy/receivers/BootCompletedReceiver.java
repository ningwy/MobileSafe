package io.github.ningwy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/**
 * 开机广播的监听类
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    private SharedPreferences sp;

    public BootCompletedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        //1.得到当前的sim卡系列号
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String simSerialNumber = tm.getSimSerialNumber();
        //2.得到保存的sim卡系列号
        SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_APPEND);
        String sim = sp.getString("sim", "") + "233333";
        //3.比较两个sim卡号
        if (simSerialNumber.equals(sim)) {
            //如果一致，什么都不用做
        } else {
            //如果不一致，把需要处理的代码写在这里，例如发送短信给设置的安全号码
            Toast.makeText(context, "sim卡变更了", Toast.LENGTH_SHORT).show();
            sendAlarmSms();
        }
    }

    /**
     * 发送报警短信给安全号码
     */
    private void sendAlarmSms() {
        String safeNumber = sp.getString("safeNumber", "");
        boolean protection = sp.getBoolean("protection", false);
        /**
         * 如果开启了手机防盗，才发送安全短信
         */
        if (protection) {
            SmsManager.getDefault().sendTextMessage(safeNumber, null, "my phone's sim card has changed!", null, null);
        }
    }
}
