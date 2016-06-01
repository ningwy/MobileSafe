package io.github.ningwy.activities;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.github.ningwy.R;
import io.github.ningwy.db.dao.QueryAddressDao;

public class QueryNumberAddressActivity extends AppCompatActivity {

    private EditText et_query_number;
    private TextView tv_query_result;

    //手机震动
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_number_address);

        init();
        et_query_number.addTextChangedListener(new TextWatcher() {
            //在文字改变之前调用
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            //在文字改变时调用
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) {
                    String address = QueryAddressDao.queryAddressFromDB(s.toString());
                    tv_query_result.setText(address);
                }

            }

            //在文字改变后调用
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    /**
     * 用于初始化
     */
    private void init() {
        et_query_number = (EditText) findViewById(R.id.et_query_number);
        tv_query_result = (TextView) findViewById(R.id.tv_query_result);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    /**
     * Button的点击事件
     * @param view
     */
    public void queryAddress(View view) {
        String number = et_query_number.getText().toString();
        if (TextUtils.isEmpty(number)) {
            Toast.makeText(getApplicationContext(), "号码不能为空", Toast.LENGTH_SHORT).show();
            //抖动动画
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.shake);
            et_query_number.startAnimation(animation);

            //手机震动
            vibrator.vibrate(500);
//            long[] pattern = {500, 1000, 1500, 2000, 1500, 1000, 500};
            //-1不重复， 0重复
//            vibrator.vibrate(pattern, -1);
        } else {
            String address = QueryAddressDao.queryAddressFromDB(number);
            tv_query_result.setText(address);
        }
    }
}
