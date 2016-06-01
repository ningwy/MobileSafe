package io.github.ningwy.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 特殊的广播接收：设备管理员权限接收者
 */
public class MyAdmin extends DeviceAdminReceiver {
    public MyAdmin() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
