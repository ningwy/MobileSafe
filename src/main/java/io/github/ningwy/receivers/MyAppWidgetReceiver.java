package io.github.ningwy.receivers;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import io.github.ningwy.services.UpdateWidgetService;

public class MyAppWidgetReceiver extends AppWidgetProvider {

    public MyAppWidgetReceiver() {
    }

    //每新创建一个widget的时候都被执行，系统默认更新时间30分，时间到了也会执行
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    //没删除一个AppWidget的时候调用
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    //删除最后一个AppWidget的时候才调用，适合释放资源，例如停止服务
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Intent intent = new Intent(context, UpdateWidgetService.class);
        context.stopService(intent);
    }

    //onEnabled方法是在创建第一个AppWidget的时候调用的，适合初始化资源，例如创建服务
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Intent intent = new Intent(context, UpdateWidgetService.class);
        context.startService(intent);
    }

    //OnReceive是每个关于AppWidget的操作都会被调用的方法,通讯就用广播
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }
}
