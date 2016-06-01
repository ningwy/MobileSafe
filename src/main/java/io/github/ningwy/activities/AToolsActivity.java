package io.github.ningwy.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

import io.github.ningwy.R;
import io.github.ningwy.utils.MD5Utils;
import io.github.ningwy.utils.SmsBackupUtils;

/**
 * 高级工具
 */
public class AToolsActivity extends AppCompatActivity {

    /**
     * 将密码输入框定义为成员变量，以便后面的监听器可以使用
     */
    AlertDialog dialog;

    private SharedPreferences sp;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Toast.makeText(getApplicationContext(), "备份完成", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atools);

        sp = getSharedPreferences("password", MODE_PRIVATE);

    }

    /**
     * 点击事件——号码归属地查询
     * @param view
     */
    public void queryNumberAddress(View view) {
        Intent intent = new Intent(this, QueryNumberAddressActivity.class);
        startActivity(intent);
    }

    /**
     * 点击事件——短信备份
     * @param view
     */
    public void smsBackup(View view) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            final File file = new File(Environment.getExternalStorageDirectory(), "smsBackup.xml");
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("正在备份中...");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.show();
            new Thread(){
                @Override
                public void run() {
                    try {
                        SmsBackupUtils.smsBackup(AToolsActivity.this, file.getAbsolutePath(), new SmsBackupUtils.SmsBackupCallBack() {
                            @Override
                            public void setCount(int count) {
                                dialog.setMax(count);
                            }

                            @Override
                            public void setProgress(int progress) {
                                dialog.setProgress(progress);
                            }
                        });
                        dialog.dismiss();
                        handler.sendEmptyMessage(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        dialog.dismiss();
                        Log.e("TAG", "发生异常啦");
                        Log.e("TAG", e.toString());
                    }
                }
            }.start();
        } else {
            Toast.makeText(getApplicationContext(), "外部存储不可用", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 常用号码查询
     * @param view
     */
    public void queryCommonNumber(View view) {
        Intent intent = new Intent(this, QueryCommonNumberActivity.class);
        startActivity(intent);
    }

    /**
     * 程序锁
     * @param view
     */
    public void applock(View view) {
        if (sp.getString("applockpsd", "").equals("")) {
            showSetupPsdDialog();
        } else {
            showEnterPsdDialog();
        }
    }

    /**
     * 显示设置密码的对话框
     */
    private void showSetupPsdDialog() {
        //加载自定义的密码输入框
        View view = View.inflate(this, R.layout.setup_psd, null);
        final EditText et_lf_psd = (EditText) view.findViewById(R.id.et_lf_psd);
        final EditText et_lf_psd_confirm = (EditText) view.findViewById(R.id.et_lf_psd_confirm);
        Button btn_lf_confirm = (Button) view.findViewById(R.id.btn_lf_confirm);
        Button btn_lf_cancel = (Button) view.findViewById(R.id.btn_lf_cancel);
        //显示自定义的密码输入框
        dialog = new AlertDialog.Builder(this).create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();

        /**
         * 两个Button按钮需要的监听器
         */
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_lf_confirm:
                        String password = et_lf_psd.getText().toString();
                        String password_confirm = et_lf_psd_confirm.getText().toString();
                        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(password_confirm)) {
                            Toast.makeText(getApplicationContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
                        } else {
                            if (password.trim().equals(password_confirm.trim())) {
                                dialog.dismiss();
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("applockpsd", MD5Utils.getMD5Secret(password));
                                editor.commit();
                                Log.e("TAG", "密码正确");
                                enterAppLockActivity();
                            } else {
                                Toast.makeText(getApplicationContext(), "密码不一致", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case R.id.btn_lf_cancel:
                        dialog.dismiss();
                        break;
                    default:
                        break;
                }
            }
        };
        //为两个Button设置点击事件
        btn_lf_confirm.setOnClickListener(listener);
        btn_lf_cancel.setOnClickListener(listener);
    }


    /**
     * 显示输入密码的对话框
     */
    private void showEnterPsdDialog() {
        //加载自定义的密码输入框
        View view = View.inflate(this, R.layout.enter_psd, null);
        final EditText et_lf_psd = (EditText) view.findViewById(R.id.et_lf_psd);
        Button btn_lf_confirm = (Button) view.findViewById(R.id.btn_lf_confirm);
        Button btn_lf_cancel = (Button) view.findViewById(R.id.btn_lf_cancel);
        //显示自定义的密码输入框
        dialog = new AlertDialog.Builder(this).create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();

        /**
         * 两个Button按钮需要的监听器
         */
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_lf_confirm:
                        String password = et_lf_psd.getText().toString();
                        String password_saved = sp.getString("applockpsd", "");
                        if (MD5Utils.getMD5Secret(password.trim()).equals(password_saved)) {
                            Toast.makeText(getApplicationContext(), "密码正确", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            enterAppLockActivity();
                        } else {
                            Toast.makeText(getApplicationContext(), "密码不正确", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.btn_lf_cancel:
                        dialog.dismiss();
                        break;
                    default:
                        break;
                }
            }
        };
        //为两个Button设置点击事件
        btn_lf_confirm.setOnClickListener(listener);
        btn_lf_cancel.setOnClickListener(listener);
    }

    /**
     * 进入程序锁页面
     */
    private void enterAppLockActivity() {
        Intent intent = new Intent(AToolsActivity.this, AppLockActivity.class);
        startActivity(intent);
    }
}
