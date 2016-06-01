package io.github.ningwy.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 程序锁数据库创建帮助类
 * Created by ningwy on 2016/5/23.
 */
public class AppLockDBOpenHelper extends SQLiteOpenHelper {

    public AppLockDBOpenHelper(Context context) {
        super(context, "applock.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //建表
        String sql = "create table applock (_id integer primary key autoincrement, packagename varchar(20))";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
