package io.github.ningwy.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.github.ningwy.R;

/**
 * 自定义设置中心里面的item视图，继承于RelativeLayout
 * Created by ningwy on 2016/4/14.
 */
public class SettingClickView extends RelativeLayout {

    private TextView tv_setting_update;
    private TextView tv_setting_state;

    private String update_on;
    private String update_off;

    /**
     * 初始化自定义布局文件里面的视图
     * @param context 上下文，
     */
    public void initView(Context context) {
        /**
         * 最后一个参数的作用是：传进来的参数就是相应的第二个参数的布局文件的父亲，
         * 也就是说，要把布局文件挂载在最后一个参数对应的视图上，this即代表当前视图。
         */
        View.inflate(context, R.layout.setting_click_view, this);

        tv_setting_update = (TextView) findViewById(R.id.tv_setting_update);
        tv_setting_state = (TextView) findViewById(R.id.tv_setting_state);
    }

    /**
     * 设置状态栏的文字
     * @param text 要设置的文字
     */
    public void setStatusText(String text) {
        tv_setting_state.setText(text);
    }

    /**
     * 设置标题
     * @param text 要设置的文字
     */
    public void setTitle(String text) {
        tv_setting_update.setText(text);
    }

    /**
     * 设置CheckBox的点击状态
     * @param checked 设置值
     */
    public void setChecked(boolean checked) {
        if (checked) {
            tv_setting_state.setText(update_on);
        } else {
            tv_setting_state.setText(update_off);
        }
    }

    /**
     * 该构造方法是在用代码实例化SettingItemView时调用的
     * @param context 上下文
     */
    public SettingClickView(Context context) {
        super(context);
        initView(context);
    }

    /**
     * 该构造方法是在Android系统实例化定义在布局文件里面的控件时调用的
     * @param context 上下文
     * @param attrs 属性集合(Map集合)
     */
    public SettingClickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        String item_title = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "item_title");
        update_on = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "update_on");
        update_off = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "update_off");

        /**
         * 设置显示文本
         */
        tv_setting_update.setText(item_title);
        /**
         * 默认显示为关闭状态，因此不需要判断
        if (isChecked()) {
            tv_setting_state.setText(update_on);
        } else {
            tv_setting_state.setText(update_off);
        }*/
        tv_setting_state.setText(update_off);
    }

    /**
     * 和第二个构造方法类似，只不过多了样式
     * @param context 上下文
     * @param attrs 属性集合(Map集合)
     * @param defStyleAttr 样式
     */
    public SettingClickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }
}
