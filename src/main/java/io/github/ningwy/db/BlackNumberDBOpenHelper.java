package io.github.ningwy.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 黑名单数据库帮助类
 * Created by ningwy on 2016/4/27.
 */
public class BlackNumberDBOpenHelper extends SQLiteOpenHelper {


    public BlackNumberDBOpenHelper(Context context) {
        super(context, "blacknumber.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //建表
        String sql = "create table blacknumber (_id integer primary key autoincrement, number varchar(20), mode varchar(2))";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
