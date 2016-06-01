package io.github.ningwy.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

import io.github.ningwy.R;

public class DragViewActivity extends AppCompatActivity {

    private TextView tv_draw_view_top;
    private ImageView iv_draw_view;
    private TextView tv_draw_view_bottom;
    private WindowManager wm;
    //屏幕宽度
    private int mWidth;
    //屏幕高度——包括状态栏高度
    private int mHeigth;
    private SharedPreferences sp;
    /**
     * 定义iv_draw_view的双击事件，可通过修改mHits的长度来实现n击事件
     */
    private float[] mHits = new float[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_view);

        init();

        iv_draw_view.setOnTouchListener(new View.OnTouchListener() {
            float startX = 0;
            float startY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float eventX = event.getRawX();
                float eventY = event.getRawY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //得到点击事件相对于屏幕左上丁点的坐标
                        startX = eventX;
                        startY = eventY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //得到偏移量
                        int dx = (int) (event.getRawX() - startX);
                        int dy = (int) (event.getRawY() - startY);
                        //得到移动后ImageView的左上和右下点的坐标
                        int newL = iv_draw_view.getLeft() + dx;
                        int newT = iv_draw_view.getTop() + dy;
                        int newR = iv_draw_view.getRight() + dx;
                        int newB = iv_draw_view.getBottom() + dy;
                        if (newL < 0 || newT < 0 || newR > mWidth || newB > mHeigth - getStatusBarHeight()) {
                            break;
                        }

                        //根据iv_draw_view的位置动态改变说明文字的透明度，
                        if (newT <= mHeigth / 2) {
                            tv_draw_view_top.setAlpha(0.0f);
                            tv_draw_view_bottom.setAlpha(1.0f);
                        } else {
                            tv_draw_view_top.setAlpha(1.0f);
                            tv_draw_view_bottom.setAlpha(0.0f);
                        }
                        iv_draw_view.layout(newL, newT, newR, newB);

                        //重新标记坐标
                        startX = eventX;
                        startY = eventY;
                        break;
                    case MotionEvent.ACTION_UP:
                        saveData();
                        break;
                }
                return false;
            }
        });

        //实现iv_draw_view的双击居中
        iv_draw_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = (int) SystemClock.uptimeMillis();
                if (mHits[0] >= SystemClock.uptimeMillis() - 500) {
                    int w = iv_draw_view.getWidth();
                    int t = iv_draw_view.getTop();
                    int b = iv_draw_view.getBottom();
                    iv_draw_view.layout(mWidth / 2 - w / 2, t, mWidth / 2 + w / 2, b);
                    saveData();
                }
            }
        });

    }

    /**
     * 保存iv_draw_view的坐标
     */
    private void saveData() {
        int left = iv_draw_view.getLeft();
        int top = iv_draw_view.getTop();
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("lastX", left);
        editor.putInt("lastY", top);
        editor.commit();
    }

    /**
     * 初始化
     */
    private void init() {
        tv_draw_view_top = (TextView) findViewById(R.id.tv_draw_view_top);
        iv_draw_view = (ImageView) findViewById(R.id.iv_draw_view);
        tv_draw_view_bottom = (TextView) findViewById(R.id.tv_draw_view_bottom);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWidth = wm.getDefaultDisplay().getWidth();
        mHeigth = wm.getDefaultDisplay().getHeight();
        sp = getSharedPreferences("config", MODE_PRIVATE);

        /**
         * 初始化iv_draw_view的位置
         */
        int left = sp.getInt("lastX", 0);
        int top = sp.getInt("lastY", 0);
        /**
         *  int right = left + iv_draw_view.getWidth();
         int bottom = top + iv_draw_view.getHeight();
         Log.e("TAG", "right:" + right);
         Log.e("TAG", "bottom:" + bottom);
         iv_draw_view.layout(left, top, right, bottom);
         */
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) iv_draw_view.getLayoutParams();
        params.leftMargin = left;
        params.topMargin = top;
        /**
         * 注意此处不能使用layout(l, t, r, b)方法来布局iv_draw_view，因为此时iv_draw_view还没有
         * 测量(onMeasure方法)，不知道大小，一个View从创建到显示需要经过的历程：
         * 1.通过构造方法实例化
         * 2.测量大小，回调onMeasure()方法
         * 3.回调onLayout()方法——layout(l, t, r, b)方法在其中被调用，因此当onMeasure()方法还没有
         * 调用时就调用layout方法是不能正常显示布局的
         * 4.回调onDraw(Canvas canvas)方法
         */
        iv_draw_view.setLayoutParams(params);

        //初始化说明文本框的位置
        if (top <= mHeigth / 2) {
            tv_draw_view_top.setAlpha(0.0f);
            tv_draw_view_bottom.setAlpha(1.0f);
        } else {
            tv_draw_view_top.setAlpha(1.0f);
            tv_draw_view_bottom.setAlpha(0.0f);
        }
    }

    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     * 导入包java.lang.reflect.Field
     */
    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        try {
            Class c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            statusBarHeight = getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }
}
