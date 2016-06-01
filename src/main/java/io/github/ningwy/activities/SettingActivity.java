package io.github.ningwy.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import io.github.ningwy.R;
import io.github.ningwy.services.AddressService;
import io.github.ningwy.services.CallSmsSafeService;
import io.github.ningwy.services.WatchDogService;
import io.github.ningwy.utils.ServiceStatusUtils;
import io.github.ningwy.views.SettingClickView;
import io.github.ningwy.views.SettingItemView;

public class SettingActivity extends AppCompatActivity {

    /**
     * 设置来电监听
     */
    private SettingItemView stv_call_listener;
    /**
     * 设置来电监听显示框的风格
     */
    private SettingClickView scv_address_style;
    /**
     * 设置来电显示框的位置
     */
    private SettingClickView scv_change_position;
    /**
     * 设置黑名单
     */
    private SettingItemView stv_black_number;
    /**
     * 设置自动更新
     */
    private SettingItemView stv_update;

    /**
     * 设置程序锁
     */
    private SettingItemView stv_app_lock;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String[] items = {"半透明", "活力橙", "卫士蓝", "金属灰", "苹果绿"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initView();
        setOnListener();

        /**
         * 设置正好点击到CheckBox按钮无效，点击事件交由stv_update来处理
         */
        stv_update.setCheckBoxUnFocused();

    }

    /**
     * 设置监听
     */
    private void setOnListener() {

        stv_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stv_update.isChecked()) {//已经点击，将设置为未点击
                    stv_update.setChecked(false);
                    editor.putBoolean("update", false);
//                    stv_update.setStatusText("应用自动升级已关闭");
                } else {//没有点击，将设置为点击状态
                    stv_update.setChecked(true);
                    editor.putBoolean("update", true);
//                    stv_update.setStatusText("应用自动升级已开启");
                }
                editor.commit();
            }
        });

        stv_call_listener.setOnClickListener(new View.OnClickListener() {
            Intent intent = new Intent(SettingActivity.this, AddressService.class);

            @Override
            public void onClick(View v) {
                if (stv_call_listener.isChecked()) {
                    stv_call_listener.setChecked(false);
                    stopService(intent);
                } else {
                    stv_call_listener.setChecked(true);
                    startService(intent);
                }
            }
        });

        scv_address_style.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = sp.getInt("style", 0);
                new AlertDialog.Builder(SettingActivity.this)
                        .setTitle("请选择一个风格")
                        .setSingleChoiceItems(items, index, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putInt("style", which);
                                editor.commit();
                                scv_address_style.setStatusText(items[which]);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create()
                        .show();
            }
        });

        scv_change_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, DragViewActivity.class);
                startActivity(intent);
            }
        });

        stv_black_number.setOnClickListener(new View.OnClickListener() {
            Intent intent = new Intent(SettingActivity.this, CallSmsSafeService.class);
            @Override
            public void onClick(View v) {
                if (stv_black_number.isChecked()) {
                    stv_black_number.setChecked(false);
                    stopService(intent);
                } else {
                    stv_black_number.setChecked(true);
                    startService(intent);
                }
            }
        });

        stv_app_lock.setOnClickListener(new View.OnClickListener() {
            Intent intent = new Intent(SettingActivity.this, WatchDogService.class);
            @Override
            public void onClick(View v) {
                if (stv_app_lock.isChecked()) {
                    stv_app_lock.setChecked(false);
                    stopService(intent);
                } else {
                    stv_app_lock.setChecked(true);
                    startService(intent);
                }
            }
        });
    }

    /**
     * 初始化视图
     */
    private void initView() {
        stv_update = (SettingItemView) findViewById(R.id.stv_update);
        stv_call_listener = (SettingItemView) findViewById(R.id.stv_call_listener);
        scv_address_style = (SettingClickView) findViewById(R.id.scv_address_style);
        scv_change_position = (SettingClickView) findViewById(R.id.scv_change_position);
        stv_black_number = (SettingItemView) findViewById(R.id.stv_black_number);
        stv_app_lock = (SettingItemView) findViewById(R.id.stv_app_lock);

        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();
        //初始化自动更新的状态提示
        boolean update = sp.getBoolean("update", false);
        /**
         if (update) {
         stv_update.setStatusText("应用自动升级已开启");
         } else {
         stv_update.setStatusText("应用自动升级已关闭");
         }
         *
         */
        stv_update.setChecked(update);
        //初始化来电监听的状态提示
        boolean isAddressService = ServiceStatusUtils.isRunningServices(this, "io.github.ningwy.services.AddressService");
        stv_call_listener.setChecked(isAddressService);
        //初始化来电监听风格的状态提示
        int index = sp.getInt("style", 0);
        scv_address_style.setStatusText(items[index]);
        //初始化黑名单设置的状态提示
        boolean isCallSmsSafeService = ServiceStatusUtils.isRunningServices(this, "io.github.ningwy.services.CallSmsSafeService");
        stv_black_number.setChecked(isCallSmsSafeService);
        //初始化程序锁设置的状态提示
        boolean isWatchDogService = ServiceStatusUtils.isRunningServices(this, "io.github.ningwy.services.WatchDogService");
        stv_app_lock.setChecked(isWatchDogService);
    }

    /**
     * 当页面转至后台之后重新回到屏幕上时，需要设置为之前保存的配置(针对服务)
     */
    @Override
    protected void onResume() {
        super.onResume();
        boolean isRunningService = ServiceStatusUtils.isRunningServices(this, "io.github.ningwy.services.AddressService");
        stv_call_listener.setChecked(isRunningService);

        boolean isCallSmsSafeService = ServiceStatusUtils.isRunningServices(this, "io.github.ningwy.services.CallSmsSafeService");
        stv_black_number.setChecked(isCallSmsSafeService);

        boolean isWatchDogService = ServiceStatusUtils.isRunningServices(this, "io.github.ningwy.services.WatchDogService");
        stv_app_lock.setChecked(isWatchDogService);
    }
}
