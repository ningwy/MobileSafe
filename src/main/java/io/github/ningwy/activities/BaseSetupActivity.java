package io.github.ningwy.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * 公共类，基类，父类
 * Created by ningwy on 2016/4/17.
 */
public abstract class BaseSetupActivity extends Activity {

    /**
     * 手势侦查器
     */
    private GestureDetector detector;

    protected SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        detector = new GestureDetector(this, new GestureDetector.OnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                /**
                 * 屏蔽慢速滑动，例如在口袋里面的摩擦慢划
                 * velocityX : 指在X轴方向的划速，单位为像素每秒(px/s)
                 * velocityY : 指在Y轴方向的划速，单位为像素每秒(px/s)
                 */
                if (Math.abs(velocityX) < 50) {
                    Toast.makeText(getApplicationContext(), "哥，划快点嘛", Toast.LENGTH_SHORT).show();
                    return true;
                }

                /**
                 * 屏蔽斜划动作
                 */
                if (Math.abs((e1.getY() - e2.getY())) > 100) {
                    Toast.makeText(getApplicationContext(), "哥，不可以斜划", Toast.LENGTH_SHORT).show();
                    return true;
                }

                /**
                 * 手势划开下一页
                 */
                if ((e1.getX() - e2.getX()) > 100) {
                    showNext();
                }

                /**
                 * 手势划开上一页
                 */
                if ((e2.getX() - e1.getX()) > 100) {
                    showPre();
                }

                return false;
            }
        });
    }

    /**
     * 打开下一页
     */
    protected abstract void showNext();

    /**
     * 打开上一页
     */
    protected abstract void showPre();

    /**
     * 将触摸事件交给手势侦查器处理
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return false;
    }

    /**
     * 按钮的点击事件：打开上一页
     * @param view
     */
    public void pre(View view) {
       showPre();
    }

    /**
     * 按钮的点击事件：打开下一页
     * @param view
     */
    public void next(View view) {
        showNext();
    }
}
