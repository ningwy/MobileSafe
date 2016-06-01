package io.github.ningwy.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.ningwy.R;
import io.github.ningwy.db.dao.BlackNumberDao;
import io.github.ningwy.domain.BlackNumberInfo;

public class CallSmsSafeActivity extends AppCompatActivity {

    private LinearLayout ll_black_number_loading;
    private TextView tv_status;
    private ListView lv_black_number;
    private BlackNumberDao dao;
    private List<BlackNumberInfo> data = new ArrayList<>();
    private CallSmsSafeAdapter adapter;
    /**
     * 用于定义ListView的分页刷新时每次需要从哪里开始加载
     */
    private int index = 0;
    /**
     * 黑名单数据库中黑名单的总数
     */
    private int dbCount;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                if (adapter == null) {
                    adapter = new CallSmsSafeAdapter();
                    lv_black_number.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
            }
            ll_black_number_loading.setVisibility(View.INVISIBLE);
            tv_status.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_sms_safe);

        ll_black_number_loading = (LinearLayout) findViewById(R.id.ll_black_number_loading);
        tv_status = (TextView) findViewById(R.id.tv_status);
        lv_black_number = (ListView) findViewById(R.id.lv_black_number);
        dao = new BlackNumberDao(getApplicationContext());
        ll_black_number_loading.setVisibility(View.VISIBLE);
        fillData();
        lv_black_number.setOnScrollListener(new AbsListView.OnScrollListener() {

            /**
             * 当滚动条状态改变时调用，有：
             * 从静止状态到滑动状态，
             * 从滑动状态到静止状态，
             * 从滑动状态状态到惯性滚动状态
             */
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    //空闲状态-静止状态
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        int count = view.getLastVisiblePosition();//19
                        int currentSize = data.size();
                        //如果下拉到当前页的最后一个数据，加载数据
                        if (count == currentSize - 1) {
                            tv_status.setVisibility(View.VISIBLE);
                            index += 20;
                            fillData();
                        }
                        //如果没有数据了，提示信息
                        if (count == dbCount - 1) {
                            Toast.makeText(getApplicationContext(), "没有数据了", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    //惯性滚动状态
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        break;
                    //手指触摸着滑动状态
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        break;

                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

    }

    /**
     * 获得黑名单数据
     */
    private void fillData() {
        new Thread() {
            @Override
            public void run() {
                if (data == null) {
                    data = dao.queryPart(index);
                } else {
                    data.addAll(dao.queryPart(index));
                }
                dbCount = dao.getCount();
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private class CallSmsSafeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return data.size();
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
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(CallSmsSafeActivity.this, R.layout.call_sms_safe_item, null);
                viewHolder = new ViewHolder();
                viewHolder.tv_number = (TextView) convertView.findViewById(R.id.tv_number);
                viewHolder.tv_mode = (TextView) convertView.findViewById(R.id.tv_mode);
                viewHolder.iv_delete_black_number = (ImageView) convertView.findViewById(R.id.iv_delete_black_number);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final BlackNumberInfo info = data.get(position);
            viewHolder.tv_number.setText(info.getNumber());
            String mode = info.getMode();
            if ("0".equals(mode)) {
                //0代表短信拦截
                viewHolder.tv_mode.setText("短信拦截");
            } else if ("1".equals(mode)) {
                //1代表电话拦截
                viewHolder.tv_mode.setText("电话拦截");
            } else {
                //2代表短信+电话拦截
                viewHolder.tv_mode.setText("短信+电话拦截");
            }

            //为删除图片添加点击事件
            viewHolder.iv_delete_black_number.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(CallSmsSafeActivity.this)
                            .setTitle("注意")
                            .setMessage("确定要删除"+ info.getNumber() +"吗")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dao.delete(info.getNumber());
                                    data.remove(info);
                                    dialog.dismiss();
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                }
            });

            return convertView;
        }
    }

    /**
     * 容器类
     */
    static class ViewHolder {
        TextView tv_number;
        TextView tv_mode;
        ImageView iv_delete_black_number;
    }

    /**
     * 添加按钮的点击事件
     *
     * @param view
     */
    public void addBlackNumber(View view) {
        //加载和初始化View
        View convertView = View.inflate(this, R.layout.add_black_number, null);
        final EditText et_black_number = (EditText) convertView.findViewById(R.id.et_black_number);
        final RadioGroup rg_mode = (RadioGroup) convertView.findViewById(R.id.rg_mode);
        Button btn_black_number_confirm = (Button) convertView.findViewById(R.id.btn_black_number_confirm);
        Button btn_black_number_cancel = (Button) convertView.findViewById(R.id.btn_black_number_cancel);
        //生成对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        dialog.setView(convertView, 0, 0, 0, 0);
        dialog.show();
        //设置点击事件
        btn_black_number_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_black_number_confirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //获得值
                String blackNumber = et_black_number.getText().toString();
                String mode = "2";
                if (TextUtils.isEmpty(blackNumber)) {
                    Toast.makeText(getApplicationContext(), "号码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                switch (rg_mode.getCheckedRadioButtonId()) {
                    //短信拦截
                    case R.id.rb_mode_sms:
                        mode = "0";
                        break;
                    //电话
                    case R.id.rb_mode_phone:
                        mode = "1";
                        break;
                    //短信+电话拦截
                    case R.id.rb_mode_all:
                        mode = "2";
                        break;
                }
                dao.add(blackNumber, mode);
                BlackNumberInfo info = new BlackNumberInfo(blackNumber, mode);
                data.add(0, info);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }

}
