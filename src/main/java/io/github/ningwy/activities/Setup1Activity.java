package io.github.ningwy.activities;

import android.content.Intent;
import android.os.Bundle;

import io.github.ningwy.R;

public class Setup1Activity extends BaseSetupActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup1);
    }

    @Override
    protected void showNext() {
        Intent intent = new Intent(Setup1Activity.this, Setup2Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
    }

    @Override
    protected void showPre() {

    }

}
