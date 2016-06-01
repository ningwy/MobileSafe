package io.github.ningwy.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 查询常用号码的dao
 * Created by ningwy on 2016/5/22.
 */
public class QueryCommonNumberDao {

    /**
     * 得到ExpandableListView的组数
     * @param db 数据库
     * @return ExpandableListView的组数
     */
    public static int getGroupCount(SQLiteDatabase db) {
        int count = 0;
        String sql = "select count(*) from classlist";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * 得到ExpandableList中父亲视图的内容
     * @param groupPosition ExpandableList 列表的位置，位置从0开始
     * @param db 数据库
     * @return ExpandableList中父亲视图的内容
     */
    public static String getGroupView(int groupPosition, SQLiteDatabase db) {
        String name = "";
        //因为数据库中idx是从1开始，而ExpandableAdapter中的方法是从0开始，故+1
        int newGroupPosition = groupPosition + 1;
        Cursor cursor = db.rawQuery("select name from classlist where idx = ?", new String[]{newGroupPosition + ""});
        while (cursor.moveToNext()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    /**
     * 得到ExpandableListView中每一组的子view数
     * @param groupPosition 组的位置——指明要哪一组的子view
     * @param db 数据库
     * @return
     */
    public static int getChildrenCount(int groupPosition, SQLiteDatabase db) {
        int count = 0;
        //因为数据库中idx是从1开始，而ExpandableAdapter中的方法是从0开始，故+1
        int newGroupPosition = groupPosition + 1;
        Cursor cursor = db.rawQuery("select count(*) from table" + newGroupPosition, null);
        while (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     *得到每一组下面子view的内容
     * @param groupPosition 组的位置
     * @param childPosition 子view的位置
     * @param db 数据库
     * @return
     */
    public static String getChildView(int groupPosition, int childPosition, SQLiteDatabase db) {
        StringBuffer buffer = new StringBuffer();
        //因为数据库中idx是从1开始，而ExpandableAdapter中的方法是从0开始，故+1
        int newGroupPosition = groupPosition + 1;
        int newChildPosition = childPosition + 1;
        //select name, number from table2 where _id = 1
        Cursor cursor = db.rawQuery("select name, number from table" + newGroupPosition + " where _id = ?", new String[]{newChildPosition + ""});
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String number = cursor.getString(1);
            buffer.append(name);
            buffer.append("\n");
            buffer.append(number);
        }
        cursor.close();
        return buffer.toString();
    }

}
