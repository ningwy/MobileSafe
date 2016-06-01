package io.github.ningwy.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import io.github.ningwy.R;
import io.github.ningwy.db.dao.QueryCommonNumberDao;

public class QueryCommonNumberActivity extends AppCompatActivity {

    private ExpandableListView elv_common_number;
    private SQLiteDatabase db;

    private static String path = "/data/data/io.github.ningwy/files/commonnum.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_common_number);

        init();

        elv_common_number.setAdapter(new QueryCommonNumberAdapter());

        elv_common_number.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                TextView tv = (TextView) v;
                //得到电话号码
                String number = tv.getText().toString().split("\n")[1].trim();
                //打开电话拨打界面
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + number));
                startActivity(intent);
                return true;
            }
        });

    }

    /**
     * 初始化
     */
    private void init() {
        elv_common_number = (ExpandableListView) findViewById(R.id.elv_common_number);
        db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
            db = null;
        }
    }

    private class QueryCommonNumberAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return QueryCommonNumberDao.getGroupCount(db);
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return QueryCommonNumberDao.getChildrenCount(groupPosition, db);
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView != null) {
                tv = (TextView) convertView;
            } else {
                tv = new TextView(QueryCommonNumberActivity.this);
            }
            tv.setPadding(15, 0, 0, 0);
            tv.setTextSize(20);
            tv.setTextColor(Color.BLACK);
            tv.setText(QueryCommonNumberDao.getGroupView(groupPosition, db));
            return tv;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView != null) {
                tv = (TextView) convertView;
            } else {
                tv = new TextView(QueryCommonNumberActivity.this);
            }
            tv.setPadding(25, 0, 0, 0);
            tv.setTextSize(16);
            tv.setTextColor(Color.GRAY);
            tv.setText(QueryCommonNumberDao.getChildView(groupPosition, childPosition, db));
            return tv;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
