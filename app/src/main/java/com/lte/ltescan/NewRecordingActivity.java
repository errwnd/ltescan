package com.lte.ltescan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NewRecordingActivity extends AppCompatActivity {

  //  public static final String OFFSET_KEY = "offset_key";
    private static final String TAG = NewRecordingActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION_START_ACTIVITY = 2;
   // private EditText mOffsetUi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_recording);

      //  mOffsetUi = findViewById(R.id.activity_new_recording_offset_ui);
      //  mOffsetUi.setText(String.format(Locale.getDefault(), "%.1f", 0.0));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void newRecordingButtonClicked(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION_START_ACTIVITY);
        }
        else {
            startRecordingActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION_START_ACTIVITY && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecordingActivity();
        }
    }

    private void startRecordingActivity() {
        Intent intent = new Intent(this, RecordActivity.class);
        double offset = 0.0;
       // try {
       //     offset = Double.parseDouble(mOffsetUi.getText().toString().trim());
      //  }
     //   catch (Exception caught) {
      //      LteLog.e(TAG, caught.getMessage(), caught);
    //    }
     //   intent.putExtra(OFFSET_KEY, offset);
        startActivity(intent);
    }
}
