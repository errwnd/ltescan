package com.lte.ltescan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class GradeFragment extends Fragment {

    private static final String DATA_READINGS_ARG = "data_readings_arg";
    private static final String FILENAME_ARG = "filename_arg";

    private ArrayList<DataReading> mDataReadings;
    private String mFilename;

    public GradeFragment() {
        // Required empty public constructor
    }

    public static GradeFragment newInstance(ArrayList<DataReading> dataReadings, String filename) {
        GradeFragment fragment = new GradeFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA_READINGS_ARG, dataReadings);
        args.putString(FILENAME_ARG, filename);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFilename = getArguments().getString(FILENAME_ARG);
            mDataReadings = (ArrayList<DataReading>) getArguments().getSerializable(DATA_READINGS_ARG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grade, container, false);
        TextView filenameText = view.findViewById(R.id.fragment_grade_filename_text_ui);
        filenameText.setText(mFilename);

        double top = 0.0, middle = 0.0, low = 0.0;
        double topProbability = 0.0, middleProbability = 0.0, lowProbability = 0.0, grade = 0.0;
        if (mDataReadings != null) {
            for (DataReading dataReading : mDataReadings) {
                int rsrp = dataReading.getRsrp();
                if (rsrp >= -95) {
                    top++;
                }
                else if (rsrp < -95 && rsrp >= -110) {
                    middle++;
                }
                else {
                    low++;
                }
            }

            int numReadings = mDataReadings.size() == 0 ? 1 : mDataReadings.size();
            topProbability = top / numReadings;
            middleProbability = middle / numReadings;
            lowProbability = low / numReadings;

            grade = 1 * lowProbability + 5.5 * middleProbability + 10 * topProbability;
        }

        TextView gradeText = view.findViewById(R.id.fragment_grade_grade_text_ui);
        gradeText.setText(new DecimalFormat("#.###").format(grade));
        TextView gradeLabel = view.findViewById(R.id.fragment_grade_grade_label_ui);

        if (top >= middle && top > low) {
            gradeText.setTextColor(getResources().getColor(R.color.fragment_grade_grade_pie_top));
            gradeLabel.setTextColor(getResources().getColor(R.color.fragment_grade_grade_pie_top));
        }
        else if (middle > low) {
            gradeText.setTextColor(getResources().getColor(R.color.fragment_grade_grade_pie_mid));
            gradeLabel.setTextColor(getResources().getColor(R.color.fragment_grade_grade_pie_mid));
        }
        else {
            gradeText.setTextColor(getResources().getColor(R.color.fragment_grade_grade_pie_low));
            gradeLabel.setTextColor(getResources().getColor(R.color.fragment_grade_grade_pie_low));
        }

        PieChart pieChart = view.findViewById(R.id.fragment_grade_pie_chart_ui);
        pieChart.setUsePercentValues(true);
       // pieChart.setDescription("");
        pieChart.setDrawHoleEnabled(false);

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        if (top != 0) {
            entries.add(new Entry((float) topProbability, 1));
            labels.add(getResources().getString(R.string.fragment_grade_pie_chart_label_top));
            colors.add(getResources().getColor(R.color.fragment_grade_grade_pie_top));
        }
        if (middle != 0) {
            entries.add(new Entry((float) middleProbability, 2));
            labels.add(getResources().getString(R.string.fragment_grade_pie_chart_label_mid));
            colors.add(getResources().getColor(R.color.fragment_grade_grade_pie_mid));
        }
        if (low != 0) {
            entries.add(new Entry((float) lowProbability, 3));
            labels.add(getResources().getString(R.string.fragment_grade_pie_chart_label_low));
            colors.add(getResources().getColor(R.color.fragment_grade_grade_pie_low));
        }

       // PieDataSet <Entry> pieDataSet = new PieDataSet<Entry>();

        PieData pieData = new PieData();
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(16f);
        pieData.setValueTextColor(getResources().getColor(R.color.fragment_grade_pie_chart_text));
        pieChart.setData(pieData);
        Legend legend = pieChart.getLegend();
        legend.setFormSize(20f);
        legend.setForm(Legend.LegendForm.SQUARE);
      //  legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        legend.setTextSize(16f);
        legend.setTextColor(getResources().getColor(R.color.fragment_grade_pie_chart_text_legend));
        legend.setXEntrySpace(20f);
       // pieDataSet.setColors(colors);

        return view;
    }
}
