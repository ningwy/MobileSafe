package io.github.ningwy.activities;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.github.ningwy.receivers.MyAdmin;

/**
 * 开启设备管理员对应的activity
 */
public class OpenAdminActivity extends AppCompatActivity {

    private ComponentName componentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        componentName = new ComponentName(this, MyAdmin.class);
        openAdmin();
        finish();
    }

    /**
     * 激活管理员权限
     */
    private void openAdmin() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "开启锁屏需要激活管理员权限");
        startActivity(intent);
    }
}
