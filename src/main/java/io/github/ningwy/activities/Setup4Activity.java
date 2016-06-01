package io.github.ningwy.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import io.github.ningwy.R;

public class Setup4Activity extends BaseSetupActivity {

    private SharedPreferences sp;
    private CheckBox cb_protection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup4);
        init();
        cb_protection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cb_protection.setText("当前状态：手机防盗已经开启");
                } else {
                    cb_protection.setText("当前状态：手机防盗已经关闭");
                }
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("protection", isChecked);
                editor.commit();
            }
        });
    }

    /**
     * 用途：初始化各种东西
     */
    private void init() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        cb_protection = (CheckBox) findViewById(R.id.cb_protection);
        boolean protection = sp.getBoolean("protection", false);
        cb_protection.setChecked(protection);
        if (protection) {
            cb_protection.setText("当前状态：手机防盗已经开启");
        } else {
            cb_protection.setText("当前状态：手机防盗已经关闭");
        }
    }

    @Override
    protected void showNext() {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("configed", true);
        editor.commit();
        Intent intent = new Intent(Setup4Activity.this, LostFindActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
    }

    @Override
    protected void showPre() {
        Intent intent = new Intent(Setup4Activity.this, Setup3Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
    }

}
