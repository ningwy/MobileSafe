package io.github.ningwy.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 自定义一个能够一直获得focused的TextView(含着金钥匙出生)
 * Created by ningwy on 2016/4/14.
 */
public class FocusedTextView extends TextView {

    /**
     * 该构造方法通常在代码中实例化TextView时用
     * @param context 上下文
     */
    public FocusedTextView(Context context) {
        super(context);
    }

    /**
     * 该构造方法通常是Android系统创建我们在xml布局文件里面定义的view时调用的
     * @param context 上下文
     * @param attrs 属性集合(Map集合)
     */
    public FocusedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置样式的时候调用的
     * @param context 上下文
     * @param attrs 属性集合(Map集合)
     * @param defStyleAttr 样式的id
     */
    public FocusedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
