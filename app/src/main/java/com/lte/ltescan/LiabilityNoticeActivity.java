package com.lte.ltescan;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LiabilityNoticeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liability_notice);

        TextView textView = findViewById(R.id.activity_liability_notice_body_ui);
        textView.setMovementMethod(new ScrollingMovementMethod());
    }
}
