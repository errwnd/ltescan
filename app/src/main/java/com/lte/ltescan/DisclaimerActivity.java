package com.lte.ltescan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class DisclaimerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);
    }

    public void liabilityNoticeActivityButtonClicked(View view) {
        Intent intent = new Intent(this, LiabilityNoticeActivity.class);
        startActivity(intent);
    }

    public void prohibitionNoticeActivityButtonClicked(View view) {
        Intent intent = new Intent(this, AcknowledgementNoticeActivity.class);
        startActivity(intent);
    }

    public void acknowledgementNoticeActivityButtonClicked(View view) {
        Intent intent = new Intent(this, ProhibitionNoticeActivity.class);
        startActivity(intent);
    }

    public void nistSoftwareActivityButtonClicked(View view) {
        Intent intent = new Intent(this, NistSoftwareActivity.class);
        startActivity(intent);
    }

    public void uncertaintyNoticeActivityButtonClicked(View view) {
        Intent intent = new Intent(this, UncertaintyNoticeActivity.class);
        startActivity(intent);
    }

    public void exitButtonClicked(View view) {
        finish();
    }

    public void acceptButtonClicked(View view) {
        Intent intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
        finish();
    }
}
