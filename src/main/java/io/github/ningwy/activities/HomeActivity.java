package io.github.ningwy.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.github.ningwy.R;
import io.github.ningwy.utils.MD5Utils;

public class HomeActivity extends AppCompatActivity {

    private GridView gv_home;
    private SharedPreferences sp;

    private final static String[] names = {
            "手机防盗", "通讯卫士", "应用管理",
            "进程管理", "流量统计", "手机杀毒",
            "缓存清理", "高级工具", "设置中心"
    };
    private final static int[] imageIds = {
            R.drawable.safe, R.drawable.callmsgsafe, R.drawable.app,
            R.drawable.taskmanager, R.drawable.netmanager, R.drawable.trojan,
            R.drawable.sysoptimize, R.drawable.atools, R.drawable.settings
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initView();
        gv_home.setAdapter(new HomeAdapter());
        gv_home.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                switch (position) {
                    //启动手机防盗
                    case 0:
                        showLostFindDialog();
                        break;

                    //打开通讯卫士
                    case 1:
                        intent = new Intent(HomeActivity.this, CallSmsSafeActivity.class);
                        startActivity(intent);
                        break;

                    //打开应用管理
                    case 2:
                        intent = new Intent(HomeActivity.this, AppManagerActivity.class);
                        startActivity(intent);
                        break;

                    //打开进程管理
                    case 3:
                        intent = new Intent(HomeActivity.this, TaskManagerActivity.class);
                        startActivity(intent);
                        break;

                    //打开手机杀毒
                    case 5:
                        intent = new Intent(HomeActivity.this, AntiVirusActivity.class);
                        startActivity(intent);
                        break;

                    //打开手机杀毒
                    case 6:
                        intent = new Intent(HomeActivity.this, CleanRubbishActivity.class);
                        startActivity(intent);
                        break;

                    //打开高级工具
                    case 7:
                        intent = new Intent(HomeActivity.this, AToolsActivity.class);
                        startActivity(intent);
                        break;

                    //启动设置中心
                    case 8:
                        intent = new Intent(HomeActivity.this, SettingActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });

    }

    /**
     * 初始化视图
     */
    private void initView() {
        gv_home = (GridView) findViewById(R.id.gv_home);
        sp = getSharedPreferences("password", MODE_PRIVATE);
    }

    /**
     * 启动进入手机防盗的页面的对话框
     */
    private void showLostFindDialog() {
        if (sp.getString("password", "").equals("")) {
            showSetupPsdDialog();
        } else {
            showEnterPsdDialog();
        }
    }

    /**
     * 将密码输入框定义为成员变量，以便后面的监听器可以使用
     */
    AlertDialog dialog;


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
                                editor.putString("password", MD5Utils.getMD5Secret(password));
                                editor.commit();
                                Log.e("TAG", "密码正确");
                                enterLostFind();
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
     * 进入手机防盗页面
     */
    private void enterLostFind() {
        Intent intent = new Intent(HomeActivity.this, LostFindActivity.class);
        startActivity(intent);
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
                        String password_saved = sp.getString("password", "");
                        if (MD5Utils.getMD5Secret(password.trim()).equals(password_saved)) {
                            Toast.makeText(getApplicationContext(), "密码正确", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            enterLostFind();
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
     * Adapter适配器
     */
    class HomeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return names.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = View.inflate(HomeActivity.this, R.layout.home_item, null);
                ViewHolder.iv_item_icon = (ImageView) convertView.findViewById(R.id.iv_item_icon);
                ViewHolder.tv_item_name = (TextView) convertView.findViewById(R.id.tv_item_name);
                convertView.setTag(new ViewHolder());
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.iv_item_icon.setImageResource(imageIds[position]);
            viewHolder.tv_item_name.setText(names[position]);

            return convertView;
        }
    }

    /**
     * Adapter里面的item的容器类，ListView(GridView)的第二层优化
     */
    static class ViewHolder {
        static ImageView iv_item_icon;
        static TextView tv_item_name;
    }

//    public void add(View view) {
//        BlackNumberDao dao = new BlackNumberDao(this);
//        Random random = new Random();
//        for (int i = 0; i < 100; i++) {
//            String number = "1881947280" + i;
//            String mode = String.valueOf(random.nextInt(3));
//            dao.add(number, mode);
//        }
//    }
}
