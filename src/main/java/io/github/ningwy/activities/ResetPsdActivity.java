package io.github.ningwy.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import io.github.ningwy.R;
import io.github.ningwy.utils.MD5Utils;

public class ResetPsdActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_reset_psd;
    private EditText et_reset_psd_confirm;
    private Button btn_reset_confirm;
    private Button btn_reset_cancel;

    private SharedPreferences sp;

    /**
     * 启动该重设密码页面的intent
     */
    private Intent intent;

    /**
     * 用flag来表示是否已经输对以往设置的密码
     * true:表示输对
     * false:表示输错或者还没输
     */
    private boolean flag;

    /**
     * intent带过来的保存在SharedPreference中的密码的名字
     */
    private String passwordName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_psd);

        init();

        btn_reset_confirm.setOnClickListener(this);
        btn_reset_cancel.setOnClickListener(this);
    }

    /**
     * 初始化
     */
    private void init() {
        et_reset_psd = (EditText) findViewById(R.id.et_reset_psd);
        et_reset_psd_confirm = (EditText) findViewById(R.id.et_reset_psd_confirm);
        btn_reset_confirm = (Button) findViewById(R.id.btn_reset_confirm);
        btn_reset_cancel = (Button) findViewById(R.id.btn_reset_cancel);

        sp = getSharedPreferences("password", MODE_PRIVATE);
        intent = getIntent();
        passwordName = intent.getStringExtra("resetpsd");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //确定
            case R.id.btn_reset_confirm :
                if (!flag) {
                    if (isCorrect()) {
                        et_reset_psd_confirm.setVisibility(View.VISIBLE);
                        et_reset_psd.setText("");
                    }
                } else {
                    reSetupPsd();
                }

                break;
            //取消
            case R.id.btn_reset_cancel :
                finish();
                break;
        }
    }

    /**
     * 重新设置密码
     */
    private void reSetupPsd() {
        String input_psd = et_reset_psd.getText().toString().trim();
        String confirm_psd = et_reset_psd_confirm.getText().toString().trim();
        if (TextUtils.isEmpty(input_psd)) {
            Toast.makeText(getApplicationContext(), "请输入密码", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(confirm_psd)) {
            Toast.makeText(getApplicationContext(), "请输入确认密码", Toast.LENGTH_SHORT).show();
        } else if (!input_psd.equals(confirm_psd)) {
            Toast.makeText(getApplicationContext(), "两次密码不一致", Toast.LENGTH_SHORT).show();
        } else if (input_psd.equals(confirm_psd)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(passwordName, MD5Utils.getMD5Secret(input_psd));
            editor.commit();
            Toast.makeText(getApplicationContext(), "设置完成，请记住新密码", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 判断输入的密码是否正确
     * true:正确
     * false:错误
     * @return
     */
    public boolean isCorrect() {
        String password = sp.getString(passwordName, "");
        String input_psd = et_reset_psd.getText().toString().trim();
        if (TextUtils.isEmpty(input_psd)) {
            Toast.makeText(getApplicationContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
        } else {
            if (password.equals(MD5Utils.getMD5Secret(input_psd))) {
                flag = true;
                return true;
            } else {
                Toast.makeText(getApplicationContext(), "密码不正确", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }
}
