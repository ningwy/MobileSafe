package io.github.ningwy.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.ningwy.R;

public class SelectContactsActivity extends AppCompatActivity {

    private LinearLayout ll_contacts_loading;
    private ListView lv_contacts;
    private List<Map<String, String>> data;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ll_contacts_loading.setVisibility(View.GONE);
            lv_contacts.setAdapter(new ContactsAdapter());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);
        init();
        lv_contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> map = data.get(position);
                String number = map.get("number");
                //回传数据
                Intent intent = new Intent();
                intent.putExtra("number", number);
                setResult(1, intent);
                finish();
            }
        });
    }

    /**
     * 用途：初始化各种东西
     */
    private void init() {
        ll_contacts_loading = (LinearLayout) findViewById(R.id.ll_contacts_loading);
        ll_contacts_loading.setVisibility(View.VISIBLE);
        lv_contacts = (ListView) findViewById(R.id.lv_contacts);
        /**
         * 当联系人很多时，会是一个相当耗时的操作，故需放在分线程里面做
         */
        new Thread(){
            @Override
            public void run() {
                data = getContacts();
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    /**
     * 得到联系人的集合
     * @return
     */
    private List<Map<String,String>> getContacts() {
        List<Map<String, String>> list = new ArrayList<>();

        ContentResolver resolver = getContentResolver();
        /**
         * 从raw_contacts表中得到联系人的contact_id，
         * 此id将用于后面作为从data表中查询具体联系人信息的查询条件
         */
        Uri raw_contacts_uri = Uri.parse("content://com.android.contacts/raw_contacts");
        Cursor cursor = resolver.query(raw_contacts_uri, new String[]{"contact_id"}, null, null, null);
        while (cursor.moveToNext()) {
            String contact_id = cursor.getString(0);
            /**
             * 注意：当删除一个联系人的时候，查到的contact_id可能为null(至于具体为什么没有把contact_id删除掉，
             * 这得看联系人应用是怎么实现的了，我们只需要知道contact_id可能为null)，所以需要判断一下，否则可能
             * 出现 IllegalArgumentException: the bind value at index 1 is null异常。我在完成该功能的时候
             * 在手机里面点击是可以看到列出来的联系人，那天晚上把一个联系人删除后第二天就怎么都打不开了，所以切记
             * 要判断一下，把为null的去掉
             */
            if (contact_id != null) {
                /**
                 * 从data表中查询具体的联系人信息，包括姓名和号码
                 */
                Uri data_uri = Uri.parse("content://com.android.contacts/data");
                /**
                 * 从data表查询了两列数据：
                 * data1列包括姓名和号码，mimetype列包括姓名和号码的类型，用于判断
                 */
                String[] items = {"data1","mimetype"};
                String[] args = {contact_id};
                Cursor dataCursor = resolver.query(data_uri, items, "raw_contact_id=?", args, null);
                /**
                 * 一个map代表一个人，因此需要在第二个while循环外面定义
                 */
                Map<String, String> map = new HashMap<>();
                while (dataCursor.moveToNext()) {
                    /**
                     * 注意：一个dataCursor有两个游标(查看data表)，也就是会循环两次，第一次会添加姓名或者号码，
                     * 第二次会添加另一个，因此需要在while循环外面添加map，否则会重复添加
                     */
                    String data1 = dataCursor.getString(0);
                    String mimetype = dataCursor.getString(1);
                    //此类型表示data1为号码
                    if ("vnd.android.cursor.item/phone_v2".equals(mimetype)) {
                        map.put("number", data1);
                        //此类型表示data1为姓名
                    } else if ("vnd.android.cursor.item/name".equals(mimetype)) {
                        map.put("name", data1);
                    }
                    /**
                     * list.add(map);
                     * 不能在这里往list里面添加map，因为这样就会重复添加，添加了两个map
                     */
                }
                dataCursor.close();
                /**
                 * 只有当number和name都不为空时才往list里面添加
                 */
                if (map.get("number") != null && map.get("name") != null) {
                    list.add(map);
                }
            }
        }
        cursor.close();

        return list;
    }

    class ContactsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(SelectContactsActivity.this, R.layout.select_contacts_item, null);
                viewHolder = new ViewHolder();
                viewHolder.tv_contacts_name = (TextView) convertView.findViewById(R.id.tv_contacts_name);
                viewHolder.tv_contacts_number = (TextView) convertView.findViewById(R.id.tv_contacts_number);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Map<String, String> item = data.get(position);
            viewHolder.tv_contacts_number.setText(item.get("number"));
            viewHolder.tv_contacts_name.setText(item.get("name"));

            return convertView;
        }
    }

    /**
     * ListView中item的view容器类
     */
    class ViewHolder {
        TextView tv_contacts_name;
        TextView tv_contacts_number;
    }

}
