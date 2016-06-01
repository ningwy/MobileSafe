package io.github.ningwy.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import io.github.ningwy.R;

public class Setup3Activity extends BaseSetupActivity {

    private EditText et_contacts_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup3);
        et_contacts_number = (EditText) findViewById(R.id.et_contacts_number);
        String safeNumber = sp.getString("safeNumber", "");
        et_contacts_number.setText(safeNumber);
    }

    /**
     * 选择联系人按钮的点击事件
     * @param view
     */
    public void selectContacts(View view) {
        Intent intent = new Intent(Setup3Activity.this, SelectContactsActivity.class);
        startActivityForResult(intent, 0);
        overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == 1 && data != null) {
            String number = data.getStringExtra("number").replaceAll("-", "").replaceAll(" ", "");
            et_contacts_number.setText(number);
        }
    }

    @Override
    protected void showNext() {
        String safeNumber = et_contacts_number.getText().toString();
        /**
         * 安全号码为空则不能点击进下一页
         */
        if (TextUtils.isEmpty(safeNumber)) {
            Toast.makeText(getApplicationContext(), "还没有设置安全号码", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("safeNumber", safeNumber);
        editor.commit();
        Intent intent = new Intent(Setup3Activity.this, Setup4Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
    }

    @Override
    protected void showPre() {
        Intent intent = new Intent(Setup3Activity.this, Setup2Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
    }
}
