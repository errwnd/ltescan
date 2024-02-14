package com.lte.ltescan;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UncertaintyNoticeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uncertainty_notice);

        TextView textView = findViewById(R.id.activity_uncertainty_notice_body_ui);
        textView.setMovementMethod(new ScrollingMovementMethod());
    }
}
