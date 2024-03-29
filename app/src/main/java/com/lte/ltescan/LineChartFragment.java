package com.lte.ltescan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;


public class LineChartFragment extends Fragment {

    private static final String DATA_READINGS_ARG = "data_readings_arg";
    private static final String FILENAME_ARG = "filename_arg";

    private ArrayList<DataReading> mDataReadings;
    private String mFilename;
    private LineChart mLineChart;

    public LineChartFragment() {
        // Required empty public constructor
    }

    public static LineChartFragment newInstance(ArrayList<DataReading> dataReadings, String filename) {
        LineChartFragment fragment = new LineChartFragment();
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
            mDataReadings = (ArrayList<DataReading>) getArguments().getSerializable(DATA_READINGS_ARG);
            mFilename = getArguments().getString(FILENAME_ARG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_line_chart, container, false);
        TextView filenameText = view.findViewById(R.id.fragment_line_chart_filename_text_ui);
        filenameText.setText(mFilename);

        mLineChart = view.findViewById(R.id.fragment_line_chart_line_chart_ui);

      //  mLineChart.setDescription("");
       // mLineChart.setNoDataTextDescription("No data");
        mLineChart.setHighlightPerTapEnabled(true);
        mLineChart.setTouchEnabled(true);

        mLineChart.setDragEnabled(true);
        mLineChart.setScaleEnabled(true);
        mLineChart.setDrawGridBackground(false);
        mLineChart.setPinchZoom(true);

        LineData lineData = new LineData();
        lineData.setValueTextColor(getResources().getColor(R.color.fragment_line_chart_line_chart_text));
        mLineChart.setData(lineData);

        Legend legend = mLineChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextColor(getResources().getColor(R.color.fragment_line_chart_line_chart_legend_text));

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setTextColor(getResources().getColor(R.color.fragment_line_chart_line_chart_x_axis_text));
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis yAxisLeft = mLineChart.getAxisLeft();
        yAxisLeft.setTextColor(getResources().getColor(R.color.fragment_line_chart_line_chart_y_axis_text));

        // This causes the axis to be limited to the range of the dataset, i.e. the default appearance is that of being
        // zoomed in to see only the data range in the data set.
        // NOTE: you have to set both values to false for this to work.
        mLineChart.getAxisLeft().setStartAtZero(false);
        mLineChart.getAxisRight().setStartAtZero(false);

        yAxisLeft.setDrawGridLines(true);
        yAxisLeft.setLabelCount(6, true);

        YAxis yAxisRight = mLineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        for (int i = 0; i < mDataReadings.size(); i++) {
            addRsrpEntry(mDataReadings.get(i).getRsrp());
        }
        mLineChart.notifyDataSetChanged();
        mLineChart.setVisibleXRange(0, 7);
        if (mDataReadings.size() > 8) {
            mLineChart.moveViewToX(mDataReadings.size() - 8);
        }

        return view;
    }

    private void addRsrpEntry(int rsrp) {
        LineData lineData = mLineChart.getData();
        if (lineData == null) {
            return;
        }

        LineDataSet lineDataSet = (LineDataSet) lineData.getDataSetByIndex(0);
        if (lineDataSet == null) {
            lineDataSet = createLineDataSet();
            lineData.addDataSet(lineDataSet);
        }
      //  lineData.addXValue("");
        lineData.addEntry(new Entry((float) rsrp, lineDataSet.getEntryCount()), 0);
    }

    protected LineDataSet createLineDataSet() {
        LineDataSet lineDataSet = new LineDataSet(null, "RSRP dBm \u00B18dB");

        // Enable this for interpreted cubic lines between data points;
        // otherwise, the lines are straight
        //lineDataSet.setDrawCubic(true);
        lineDataSet.setCubicIntensity(0.2f);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setColor(getResources().getColor(R.color.fragment_line_chart_line_chart_line_color));
        lineDataSet.setCircleColor(getResources().getColor(R.color.fragment_line_chart_line_chart_circle_color));
       // lineDataSet.setCircleColorHole(getResources().getColor(R.color.fragment_line_chart_line_chart_circle_hole_color));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(6f);
        //lineDataSet.setFillColor(Color.YELLOW);
        lineDataSet.setHighLightColor(getResources().getColor(R.color.fragment_line_chart_line_chart_highlight_color));
        lineDataSet.setValueTextColor(getResources().getColor(R.color.fragment_line_chart_line_chart_value_text_color));
        lineDataSet.setValueTextSize(10f);

        return lineDataSet;
    }
}
