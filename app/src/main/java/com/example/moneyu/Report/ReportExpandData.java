package com.example.moneyu.Report;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneyu.Adapter.ReportAdapter;
import com.example.moneyu.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReportExpandData extends AppCompatActivity {
    private TextView date;
    private Map<String, Double> categoryTotalMap=null;
    private String dateToShow;
    private ImageView backIcon;
    private PieChart pieChart;
    private RecyclerView recyclerview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_expand);

//        date = findViewById(R.id.report_expanded_text);
        backIcon = findViewById(R.id.menu_icon);
        pieChart = findViewById(R.id.pieChart);
        recyclerview = findViewById(R.id.report_expanded_recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Retrieve the categoryTotalMap and date from the intent extras
        categoryTotalMap = (Map<String, Double>) getIntent().getSerializableExtra("categoryTotalMap"); // Use "categoryTotalMap"
        dateToShow = getIntent().getStringExtra("currentDate");

        date.setText(dateToShow);
        // Populate the recyclerview
        ReportAdapter adapter = new ReportAdapter(this, new ArrayList<>(categoryTotalMap.entrySet()));
        recyclerview.setAdapter(adapter);


        // Map categories to colors
        Map<String, Integer> categoryColors = new HashMap<>();
        categoryColors.put("Lương và Phụ cấp",Color.parseColor("#114232"));
        categoryColors.put("Kinh doanh", Color.parseColor("#800000"));
        categoryColors.put("Đầu tư", Color.parseColor("#9E9E9E"));
        categoryColors.put("Quà tặng", Color.parseColor("#FFA500"));
        categoryColors.put("Đồ ăn", Color.parseColor("#FFC0CB"));
        categoryColors.put("Di chuyển", Color.parseColor("#0000FF"));
        categoryColors.put("Giải trí", Color.parseColor("#008000"));
        categoryColors.put("Sức khoẻ và làm đẹp", Color.parseColor("#FF000000"));
        categoryColors.put("Thuốc thang", Color.parseColor("#000080"));
        categoryColors.put("Bảo hiểm", Color.parseColor("#A52A2A"));
        categoryColors.put("Mua sắm", Color.parseColor("#FFCC33"));
        categoryColors.put("Du lịch", Color.parseColor("#FFFF00"));
        categoryColors.put("Khác", Color.parseColor("#000000"));
        // categoryTotalMap contains the total amount for each income category
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        double totalAmount = 0;
        if (categoryTotalMap != null) {
            // Calculate total amount
            for (Double amount : categoryTotalMap.values()) {
                totalAmount += amount;
            }

            for (Map.Entry<String, Double> entry : categoryTotalMap.entrySet()) {
                String category = entry.getKey();
                Double categoryAmount = entry.getValue();

                // Calculate percentage
                float percentage = (float) ((categoryAmount / totalAmount) * 100);

                // Log the category and percentage
                Log.d(TAG, "Category: " + category + ", Percentage: " + percentage);

                // Add data to entries and colors lists
                entries.add(new PieEntry(percentage, category));
                colors.add(categoryColors.get(category));
            }

            // After iterating through the map, set up the pie chart
            setupPieChart(pieChart, entries, colors);
        } else {
            // Handle the case where categoryTotalMap is null
            Log.e(TAG, "categoryTotalMap is null");
        }
    }

    private void setupPieChart(final PieChart pieChart, ArrayList<PieEntry> entries, ArrayList<Integer> colors) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawValues(true); // Enable value labels
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD); // Set value label text style to bold
        dataSet.setColors(colors); // Retrieve colors
        dataSet.setSliceSpace(3f); // Space between slices
        dataSet.setSelectionShift(5f); // Shift distance for selected slice

        PieData data = new PieData(dataSet); // Create PieData
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTypeface(Typeface.DEFAULT_BOLD); // Set label text style to bold
        pieChart.setData(data); // Set data to PieChart
        pieChart.getDescription().setEnabled(false); // Remove description label
        pieChart.getLegend().setEnabled(false); // Remove legend
        pieChart.setDrawEntryLabels(false); // Disable the display of category names (labels)
        pieChart.setRotationEnabled(false);
        pieChart.invalidate(); // Refresh the chart

        // Custom ValueFormatter to add '%' symbol
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry pieEntry) {
                return (int) value + "%";
            }
        });

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e == null)
                    return;
                PieEntry entry = (PieEntry) e;
                String label = entry.getLabel();
                float value = entry.getValue();

                // Retrieve the amount for the selected category
                Double amount = categoryTotalMap.get(label);

                pieChart.setCenterText(label + ": " + amount + "VNĐ"); // Display label and amount in the center of the chart
                pieChart.invalidate(); // Refresh the chart
            }


            @Override
            public void onNothingSelected() {
                pieChart.setCenterText(""); // Display nothing in the center of the chart
            }
        });
    }
}
