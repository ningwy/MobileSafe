package io.github.ningwy.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.ningwy.R;

public class LostFindActivity extends AppCompatActivity {

    private SharedPreferences sp;
    private TextView tv_safe_number;
    private ImageView iv_protection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        if (sp.getBoolean("configed", false)) {
            setContentView(R.layout.activity_lost_find);
            tv_safe_number = (TextView) findViewById(R.id.tv_safe_number);
            iv_protection = (ImageView) findViewById(R.id.iv_protection);
            showFullData();
        } else {
            enterSetting();
        }
    }

    /**
     * 填充页面数据，例如安全号码，是否开启防盗保护对应的图片
     */
    private void showFullData() {
        //1.填充安全号码
        String safeNumber = sp.getString("safeNumber", "");
        tv_safe_number.setText(safeNumber);
        //2.根据是否开启防盗保护设置对应的图片
        boolean protection = sp.getBoolean("protection", false);
        if (protection) {
            iv_protection.setImageResource(R.drawable.lock);
        } else {
            iv_protection.setImageResource(R.drawable.unlock);
        }
    }

    /**
     * 进入设置界面
     */
    private void enterSetting() {
        Intent intent = new Intent(LostFindActivity.this, Setup1Activity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 重新进入设置界面
     * @param view
     */
    public void reEnterSetting(View view) {
        enterSetting();
    }
}
