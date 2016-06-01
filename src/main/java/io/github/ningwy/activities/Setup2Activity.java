package io.github.ningwy.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import io.github.ningwy.R;
import io.github.ningwy.views.SettingItemView;

public class Setup2Activity extends BaseSetupActivity {

    private SettingItemView siv_bind_sim;
    private TelephonyManager tm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup2);
        siv_bind_sim = (SettingItemView) findViewById(R.id.siv_bind_sim);
        /**
         * 得到电话管理器
         */
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        /**
         * 得到sim卡系列号
         */
        final String simSerialNumber = tm.getSimSerialNumber();
        /**
         * 从SharedPreferences中得到保存的sim系列号
         */
        String sim = sp.getString("sim", "");

        /**
         * 如果从SharedPreferences中的系列号不为空，则将siv_bind_sim设置为勾选上
         */
        if (TextUtils.isEmpty(sim)) {
            siv_bind_sim.setChecked(false);
        } else {
            siv_bind_sim.setChecked(true);
        }

        siv_bind_sim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sp.edit();
                if (siv_bind_sim.isChecked()) {
                    //如果已经勾选，设置为未勾选，并在SharedPreferences中保存为""
                    siv_bind_sim.setChecked(false);
                    editor.putString("sim","");
                } else {
                    //如果没有勾选，设置为勾选，并在SharedPreferences中保存为sim卡系列号
                    siv_bind_sim.setChecked(true);
                    editor.putString("sim", simSerialNumber);
                }
                editor.commit();
            }
        });

    }

    @Override
    protected void showNext() {

        /**
         * 如果没有绑定sim卡，禁止进入下一页
         */
        if (!siv_bind_sim.isChecked()) {
            Toast.makeText(getApplicationContext(), "请绑定sim卡，否则下面的服务无法使用", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Setup2Activity.this, Setup3Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
    }

    @Override
    protected void showPre() {
        Intent intent = new Intent(Setup2Activity.this, Setup1Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
    }

}
