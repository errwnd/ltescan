package com.lte.ltescan;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.lte.ltescan.util.LteLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DisplayResultsActivity extends AppCompatActivity {

    private ArrayList<DataReading> mDataReadings;
  //  private double mOffset;
    private String mCsvFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_results);
        mDataReadings = (ArrayList<DataReading>) getIntent().getSerializableExtra(RecordActivity.DATA_READINGS_KEY);
      //  mOffset = getIntent().getDoubleExtra(RecordActivity.OFFSET_KEY, 0.0);

        mCsvFilename = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        mCsvFilename = mCsvFilename.replace(' ', '_').replace(",", "");

        ViewPager viewPager = findViewById(R.id.activity_display_results_view_pager_ui);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(GradeFragment.newInstance(mDataReadings, mCsvFilename), getString(R.string.activity_display_results_grade_tab_text));
        adapter.addFragment(LineChartFragment.newInstance(mDataReadings, mCsvFilename), getString(R.string.activity_display_results_line_chart_tab_text));
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.activity_display_results_tab_layout_ui);
        tabLayout.setupWithViewPager(viewPager);
        writeCsv();
    }

    protected void writeCsv() {

        // Write CSV file in a background thread.
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Writer writer = null;
                try {
                    writer = new OutputStreamWriter(new FileOutputStream(new File(getExternalFilesDir(null), mCsvFilename + ".csv")), StandardCharsets.UTF_8);
                 //   writer.write("\"Time\",\"RSRP\",\"RSRQ\",\"SINR\",\"OFFSET=" + mOffset + "\"\n");
                    for (DataReading dataReading : mDataReadings) {
                        String timestamp = DateFormat.getDateTimeInstance().format(dataReading.getTimestamp());
                        writer.write(String.format("\"%s\",\"%d\",\"%d\",\"%s\"%n", timestamp, dataReading.getRsrp(), dataReading.getRsrq(),dataReading.getRssi(), dataReading.getSinr() == -1 ? "N/A" : dataReading.getSinr() + ""));
                    }
                    Toast.makeText(DisplayResultsActivity.this, "CSV file written", Toast.LENGTH_SHORT).show();

                }
                catch (IOException caught) {
                    LteLog.e("CSV Writer", caught.getMessage(), caught);
                    Toast.makeText(DisplayResultsActivity.this, "Error writing CSV file", Toast.LENGTH_SHORT).show();
                }
                finally {
                    if (writer != null) {
                        try {
                            writer.flush();
                            writer.close();
                        }
                        catch (IOException ignore) {}
                    }
                }
            }
        }, 0);
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragments;
        private final List<String> mFragmentTitles;

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
            mFragments = new ArrayList<>();
            mFragmentTitles = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }

        private void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }
    }
}
