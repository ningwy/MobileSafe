package io.github.ningwy.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import io.github.ningwy.R;
import io.github.ningwy.services.KillProcessService;
import io.github.ningwy.utils.ServiceStatusUtils;

public class TaskManagerSettingActivity extends AppCompatActivity {

    private CheckBox cb_show_sys;
    private CheckBox cb_kill_process;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_manager_setting);
        
        init();

        cb_show_sys.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sp.edit();
                if (isChecked) {
                    cb_show_sys.setText("当前状态:显示系统进程");
                    editor.putBoolean("showSystemProcess", true);
                } else {
                    cb_show_sys.setText("当前状态:关闭显示系统进程");
                    editor.putBoolean("showSystemProcess", false);
                }
                editor.commit();
            }
        });

        cb_kill_process.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent(TaskManagerSettingActivity.this, KillProcessService.class);
                SharedPreferences.Editor editor = sp.edit();
                if (isChecked) {
                    cb_kill_process.setText("当前状态:开启锁屏清理后台进程");
                    startService(intent);
                    editor.putBoolean("killProcess", true);
                } else {
                    cb_kill_process.setText("当前状态:锁屏清理后台进程关闭");
                    stopService(intent);
                    editor.putBoolean("killProcess", false);
                }
                editor.commit();
            }
        });
        
    }

    /**
     * 初始化
     */
    private void init() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        cb_show_sys = (CheckBox) findViewById(R.id.cb_show_sys);
        cb_kill_process = (CheckBox) findViewById(R.id.cb_kill_process);

        boolean showSystemProcess = sp.getBoolean("showSystemProcess", true);
        cb_show_sys.setChecked(showSystemProcess);
        if (showSystemProcess) {
            cb_show_sys.setText("当前状态:显示系统进程");
        } else {
            cb_show_sys.setText("当前状态:关闭显示系统进程");
        }

        boolean isServiceRunning = ServiceStatusUtils.isRunningServices(TaskManagerSettingActivity.this,
                "io.github.ningwy.services.KillProcessService");
        boolean killProcess = sp.getBoolean("killProcess", true);
        cb_kill_process.setChecked(killProcess);
        if (killProcess && isServiceRunning) {
            cb_kill_process.setText("当前状态:开启锁屏清理后台进程");
        } else {
            cb_kill_process.setText("当前状态:锁屏清理后台进程关闭");
        }
    }
}
