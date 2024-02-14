
package com.lte.ltescan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.lte.ltescan.util.LteLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RecordActivity extends AppCompatActivity {

    public static final String DATA_READINGS_KEY = "data_readings_key";
   // public static final String OFFSET_KEY = "offset_key";

    private static final String TAG = RecordActivity.class.getSimpleName();
    private static final Object MUTEX = new Object();

    private Button mPauseRecordButton;
    private ImageView mRecordingImage;
    private TextView mRecordingImageLabel;
    private AlphaAnimation mRecordingImageAnimation;
    private SignalStrengthListener mSignalStrengthListener;
    private TextView mRsrpText, mRsrqText, mSinrText,mRssiText, mDataPointsText, mOffsetText, mSignalStrengthText;
    private DataReading mCurrentReading;
  //  private double mOffset;
    private Timer mTimer;
    private List<DataReading> mDataReadings;
    private AtomicInteger mTicksSinceLastCellInfoUpdate;
    private AtomicBoolean mCellInfoRefresh;

    @SuppressLint({"MissingInflatedId", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        mCurrentReading = new DataReading();
        mDataReadings = new ArrayList<>();

      //  mOffset = getIntent().getDoubleExtra(NewRecordingActivity.OFFSET_KEY, 0.0);

        mPauseRecordButton = findViewById(R.id.activity_record_pause_resume_button_ui);
        mRecordingImage = findViewById(R.id.activity_record_record_image_ui);
        mRecordingImageLabel = findViewById(R.id.activity_record_record_image_label_ui);
        mRsrpText = findViewById(R.id.activity_record_lte_rsrp_text_ui);
        mRsrqText = findViewById(R.id.activity_record_lte_rsrq_text_ui);
        mSinrText = findViewById(R.id.activity_record_lte_sinr_text_ui);
        mDataPointsText = findViewById(R.id.activity_record_data_points_text_ui);
        mRssiText = findViewById(R.id.activity_record_lte_rssi_text_ui);
        mSignalStrengthText = findViewById(R.id.activity_record_signal_strength_text_ui);

        // Make part of text clickable.
        ClickableSpan clickableSpan = new ClickableSpan() {

            @Override
            public void onClick(@NonNull View view) {
                Intent intent = new Intent(RecordActivity.this, UncertaintyNoticeActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint textPaint) {
                super.updateDrawState(textPaint);
                textPaint.setColor(getResources().getColor(R.color.activity_record_clickable_color));


            }
        };
        TextView rsrpLabel = findViewById(R.id.activity_record_lte_rsrp_label_ui);
        SpannableString rsrpSpan = new SpannableString(getString(R.string.activity_record_lte_rsrp_label_text));
        rsrpSpan.setSpan(clickableSpan, rsrpSpan.length() - 6, rsrpSpan.length(), 0);
        rsrpLabel.setMovementMethod(LinkMovementMethod.getInstance());
        rsrpLabel.setText(rsrpSpan);

        TextView rsrqLabel = findViewById(R.id.activity_record_lte_rsrq_label_ui);
        SpannableString rsrqSpan = new SpannableString(getString(R.string.activity_record_lte_rsrq_label_text));
        rsrqSpan.setSpan(clickableSpan, rsrqLabel.length() - 6, rsrqLabel.length(), 0);
        rsrqLabel.setMovementMethod(LinkMovementMethod.getInstance());
        rsrqLabel.setText(rsrqSpan);


        TextView sinrLabel = findViewById(R.id.activity_record_lte_sinr_label_ui);
        SpannableString sinrSpan = new SpannableString(getString(R.string.activity_record_lte_sinr_label_text));
        sinrSpan.setSpan(clickableSpan, sinrLabel.length() - 6, sinrLabel.length(), 0);
        sinrLabel.setMovementMethod(LinkMovementMethod.getInstance());
        sinrLabel.setText(sinrSpan);

        TextView rssiLabel = findViewById(R.id.activity_record_lte_rssi_text_ui);
        SpannableString rssiSpan = new SpannableString(getString(R.string.activity_record_lte_rssi_label_text));
        rssiSpan.setSpan(clickableSpan, rssiLabel.length()  , rssiLabel.length(), 0);
        rssiLabel.setMovementMethod(LinkMovementMethod.getInstance());
        rssiLabel.setText(rssiSpan);

        mRecordingImageAnimation = new AlphaAnimation(1, 0);
        mRecordingImageAnimation.setDuration(750);
        mRecordingImageAnimation.setInterpolator(new LinearInterpolator());
        mRecordingImageAnimation.setRepeatCount(Animation.INFINITE);
        mRecordingImageAnimation.setRepeatMode(Animation.REVERSE);

        mSignalStrengthListener = new SignalStrengthListener();

        // Specifically for AndroidX and higher. Should call a refresh of cell info, otherwise there might be stale data.
        // See getAllCellInfo() docs from TelephonyManager.
        mTicksSinceLastCellInfoUpdate = new AtomicInteger(0);
        mCellInfoRefresh = new AtomicBoolean(true);

        // Now do the recording. We want to keep recording in the background so start and stop
        // in onCreate and onDestroy.
        ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(mSignalStrengthListener, SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);
        setResumeRecordingState();

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                final DataReading dataReadingCopy;
                synchronized (MUTEX) {

                    // To be used on the UI thread.
                    dataReadingCopy = new DataReading(mCurrentReading);
                }

                try {
                    if (ActivityCompat.checkSelfPermission(RecordActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                        List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();
                        if (allCellInfo != null) {
                            for (CellInfo cellInfo : allCellInfo) {
                                LteLog.i(TAG, cellInfo.toString());
                                if (cellInfo.isRegistered() && cellInfo instanceof CellInfoLte) {
                                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                                    CellSignalStrengthLte signalStrengthLte = cellInfoLte.getCellSignalStrength();

                                    // Overwrite the SignalStrengthListener values if build is greater than 26 and only the rsrp if value less than 26.
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        dataReadingCopy.setRsrp(signalStrengthLte.getRsrp());
                                        dataReadingCopy.setRsrq(signalStrengthLte.getRsrq());
                                        dataReadingCopy.setSinr(signalStrengthLte.getRssnr());
                                        dataReadingCopy.setRssi(signalStrengthLte.getRssi());
                                        LteLog.i(TAG, "(VERSION >= 26) rsrp: " + signalStrengthLte.getRsrp() + ", rsrq: " + signalStrengthLte.getRsrq() + ", sinr: " +signalStrengthLte.getRssnr() + " , rssi: " + signalStrengthLte.getRssi() );
                                    }
                                    else {
                                        dataReadingCopy.setRsrp(signalStrengthLte.getDbm()); // dbm = rsrp for values less than build 26.
                                        LteLog.i(TAG, String.format(Locale.getDefault(), "(VERSION < 26) rsrp: %d", signalStrengthLte.getDbm()));
                                    }

                                    // Now get the pci.
                                    CellIdentityLte identityLte = cellInfoLte.getCellIdentity();
                                    if (identityLte != null) {
                                        int sinr = identityLte.getPci();
                                        if (sinr == DataReading.UNAVAILABLE) {
                                            sinr = -1;
                                        }
                                        dataReadingCopy.setSinr(sinr);
                                    }
                                }
                            }
                        }

                        // Anything larger than AndroidX, call for a refresh of the cell info.
                        // See getAllCellInfo() docs from TelephonyManager.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && mTicksSinceLastCellInfoUpdate.getAndIncrement() >= 10 && mCellInfoRefresh.get()) {

                            LteLog.i(TAG,"Requesting refreshed cell info on AndroidX or higher");
                            mCellInfoRefresh.set(false);
                            telephonyManager.requestCellInfoUpdate(getMainExecutor(), new TelephonyManager.CellInfoCallback() {

                                @Override
                                public void onCellInfo(List<CellInfo> cellInfos) {
                                    LteLog.i(TAG,"Getting refreshed cell info on AndroidX or higher");
                                    mTicksSinceLastCellInfoUpdate.set(0);
                                    mCellInfoRefresh.set(true);
                                }
                            });
                        }
                    }
                }
                catch (Exception caught) {
                    LteLog.e(TAG, caught.getMessage(), caught);
                }

                // Adjust the rsrp;
                if (dataReadingCopy.getRsrp() == DataReading.UNAVAILABLE) {
                    dataReadingCopy.setRsrp(DataReading.LOW_RSRP);
                }
                else {
                    dataReadingCopy.setRsrp(dataReadingCopy.getRsrp() );
                }

                // Adjust the rsrq.
                if (dataReadingCopy.getRsrq() == DataReading.UNAVAILABLE) {
                    dataReadingCopy.setRsrq(DataReading.LOW_RSRQ);
                }

                if (isRecording()) {
                    mDataReadings.add(new DataReading(dataReadingCopy));
                }

                // To be used on the UI thread.
                final int numDataReadings = mDataReadings.size() == 0 ? 1 : mDataReadings.size();

                runOnUiThread(() -> {
                    if (isRecording()) {
                        if (dataReadingCopy.getRsrp() >= DataReading.EXECELLENT_RSRP_THRESHOLD) {
                            mSignalStrengthText.setText(getResources().getString(R.string.activity_record_signal_strength_excellent));
                        }
                        else if (DataReading.EXECELLENT_RSRP_THRESHOLD > dataReadingCopy.getRsrp() && dataReadingCopy.getRsrp() >= DataReading.GOOD_RSRP_THRESHOLD) {
                            mSignalStrengthText.setText(getResources().getString(R.string.activity_record_signal_strength_good));
                        }
                        else if (DataReading.GOOD_RSRP_THRESHOLD > dataReadingCopy.getRsrp() && dataReadingCopy.getRsrp() >= DataReading.POOR_RSRP_THRESHOLD) {
                            mSignalStrengthText.setText(getResources().getString(R.string.activity_record_signal_strength_poor));
                        }
                        else {
                            mSignalStrengthText.setText(getResources().getString(R.string.activity_record_signal_strength_no_signal));
                        }
                        mRsrpText.setText(String.format(Locale.getDefault(), "%d", dataReadingCopy.getRsrp()));
                        mRsrqText.setText(String.format(Locale.getDefault(), "%d", dataReadingCopy.getRsrq()));

                        mSinrText.setText(dataReadingCopy.getSinr() == -1 ? "N/A" : String.format(Locale.getDefault(), "%d", dataReadingCopy.getSinr()));
                        mDataPointsText.setText(String.format(Locale.getDefault(), "%d", numDataReadings));
                        mRssiText.setText(String.format(Locale.getDefault(), "%d", dataReadingCopy.getRssi()));
                       // mOffsetText.setText(String.format(Locale.getDefault(), "%.1f", mOffset));
                    }
                });
            }
        }, 1000, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mSignalStrengthListener != null) {
                ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(mSignalStrengthListener, SignalStrengthListener.LISTEN_NONE);
            }
        }
        catch (Exception caught) {
            LteLog.e(TAG, caught.getMessage(), caught);
        }

        mTimer.cancel();
        mTimer.purge();
        mTimer = null;
    }

   /* @Override
    public void onBackPressed() {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Do you want to cancel recording? All data will be lost.")
                .setPositiveButton("YES", (dialog, which) -> finish())
                .setNegativeButton("NO", (dialog, which) -> dialog.dismiss())
                .create();
        alert.show();

    }
*/


    public void pauseRecordButtonClicked(View view) {
        if (isRecording()) {
            setPauseRecordingState();
        }
        else {
            setResumeRecordingState();
        }
    }

    public void stopButtonClicked(View view) {
        Intent intent = new Intent(this, DisplayResultsActivity.class);
        intent.putExtra(DATA_READINGS_KEY, (ArrayList<DataReading>) mDataReadings);
        //intent.putExtra(OFFSET_KEY, mOffset);
        startActivity(intent);
        finish();
    }

    public void uncertaintyStatementButtonClicked(View view) {
        Intent intent = new Intent(this, UncertaintyNoticeActivity.class);
        startActivity(intent);
    }

    private boolean isRecording() {
        return getString(R.string.activity_record_pause_button_text).equals(mPauseRecordButton.getText().toString());
    }

    private void setPauseRecordingState() {
        mPauseRecordButton.setText(getString(R.string.activity_record_resume_button_text));
        mPauseRecordButton.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_new_recording), null, null, null);
        mRecordingImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_recording_paused));
        mRecordingImageLabel.setText(R.string.activity_record_recording_paused_image_label_text);
        mRecordingImage.clearAnimation();
    }

    private void setResumeRecordingState() {
        mPauseRecordButton.setText(getString(R.string.activity_record_pause_button_text));
        mPauseRecordButton.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_pause), null, null, null);
        mRecordingImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_recording));
        mRecordingImageLabel.setText(R.string.activity_record_record_image_label_text);
        mRecordingImage.startAnimation(mRecordingImageAnimation);
    }

    private class SignalStrengthListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            LteLog.i(TAG, "onSignalStrengthsChanged: " + signalStrength.toString());

            // AndroidX changed the format of the signal strength.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                String[] values = signalStrength.toString().split(" ");
                if (values != null && values.length > 12) {
                    try {
                        int rsrp = Integer.parseInt(values[9]);
                        int rsrq = Integer.parseInt(values[10]);
                        int rssi =Integer.parseInt(values[9]);
                        synchronized (MUTEX) {
                            mCurrentReading.setRsrp(rsrp);
                            mCurrentReading.setRsrq(rsrq);
                            mCurrentReading.setRssi(rssi);
                            mCurrentReading.setSinr(DataReading.PCI_NA);
                        }
                        LteLog.i(TAG, String.format(Locale.getDefault(), "rsrp: %d, rsrq: %d, rssi : %d", rsrp, rsrq, rssi ));
                    }
                    catch (Exception caught) {
                        LteLog.i(TAG, "Caught an error parsing signal strength");
                    }
                }
            }
            else {
                List<CellSignalStrength> cellSignalStrengths = signalStrength.getCellSignalStrengths();
                if (cellSignalStrengths != null) {
                    for (CellSignalStrength cellSignalStrength : cellSignalStrengths) {
                        if (cellSignalStrength instanceof CellSignalStrengthLte) {
                            synchronized (MUTEX) {
                                mCurrentReading.setRsrp(((CellSignalStrengthLte) cellSignalStrength).getRsrp());
                                mCurrentReading.setRsrq(((CellSignalStrengthLte) cellSignalStrength).getRsrq());
                               mCurrentReading.setSinr(((CellSignalStrengthLte) cellSignalStrength).getRssnr());
                               mCurrentReading.setRssi(((CellSignalStrengthLte) cellSignalStrength).getRssi());
                            }
                        }
                    }
                }
            }

            super.onSignalStrengthsChanged(signalStrength);
        }
    }
}
