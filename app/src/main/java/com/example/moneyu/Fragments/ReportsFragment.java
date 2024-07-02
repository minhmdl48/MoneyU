package com.example.moneyu.Fragments;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moneyu.Models.Transaction;
import com.example.moneyu.R;
import com.example.moneyu.Report.ReportExpandData;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsFragment extends Fragment {
    private PieChart incomesPieChart, expensesPieChart;
    private BarChart barChart;
    private TextView expenseReport, incomesReport;
    private FirebaseFirestore db;
    private String currentMonth = "";
    private String currentYear = "";

    public ReportsFragment() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        incomesPieChart = view.findViewById(R.id.incomesPieChart);
        expensesPieChart = view.findViewById(R.id.expensesPieChart);
        expenseReport = view.findViewById(R.id.expenseReport_textview);
        incomesReport = view.findViewById(R.id.incomesReport_textview);
        barChart = view.findViewById(R.id.income_expense_barChart);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;
        Calendar calendar = Calendar.getInstance();

        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        currentMonth = String.format("%02d", month);
        currentYear = String.valueOf(year);

        if (userId != null) {
            retrieveBalance(currentMonth, currentYear, userId);
        } else {
            Log.d("Debug", "User ID is null");
        }
    }

    private void retrieveBalance(String currentMonth, String currentYear, String userId) {
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Transaction> transactions = new ArrayList<>();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Transaction transaction = document.toObject(Transaction.class);
                                String[] dateParts = transaction.getDate().split("-");
                                if (dateParts.length == 3) {
                                    String month = dateParts[1];
                                    String year = dateParts[2];

                                    if (month.equals(currentMonth) && year.equals(currentYear)) {
                                        transactions.add(transaction);
                                    }
                                }
                            }
                        }

                        processPieCharts(transactions);
                        setupBarChart(transactions);
                    } else {
                        // Handle the error
                        System.out.println("Error getting documents: " + task.getException());
                    }
                });

    }

    private void processPieCharts(List<Transaction> transactions) {
        if (transactions == null) {
            Log.e(TAG, "Transactions list is null");
            return;
        }

        List<Transaction> expenseTransactions = new ArrayList<>();
        List<Transaction> incomeTransactions = new ArrayList<>();

        // Map categories to colors
        Map<String, Integer> categoryColors = new HashMap<>();
        categoryColors.put("Lương", Color.parseColor("#114232"));
        categoryColors.put("Bán đồ", Color.parseColor("#800000"));
        categoryColors.put("Tiền lãi", Color.parseColor("#9E9E9E"));
        categoryColors.put("Đồ ăn", Color.parseColor("#FFC0CB"));
        categoryColors.put("Di chuyển", Color.parseColor("#0000FF"));
        categoryColors.put("Ăn uống", Color.parseColor("#008000"));
        categoryColors.put("Sức khoẻ", Color.parseColor("#FF000000"));
        categoryColors.put("Bảo hiểm", Color.parseColor("#A52A2A"));
        categoryColors.put("Thu nhập khác", Color.parseColor("#000000"));
        categoryColors.put("Hóa đơn", Color.parseColor("#FF4500"));
        categoryColors.put("Mua sắm", Color.parseColor("#FFCC33"));
        categoryColors.put("Du lịch", Color.parseColor("#FFFF00"));
        categoryColors.put("Chi phí khác", Color.parseColor("#000000"));

        // Separate transactions based on their type
        for (Transaction transaction : transactions) {
            if ("Expense".equals(transaction.getType())) {
                expenseTransactions.add(transaction);
            } else if ("Income".equals(transaction.getType())) {
                incomeTransactions.add(transaction);
            }
        }

        // Calculate total amount for each expense category
        Map<String, Integer> categoryTotalExpensesMap = new HashMap<>();
        for (Transaction expenseTransaction : expenseTransactions) {
            String category = expenseTransaction.getCategory();
            int amount = expenseTransaction.getAmount();
            categoryTotalExpensesMap.put(category, categoryTotalExpensesMap.getOrDefault(category, 0) + amount);
        }

        Map<String, Integer> categoryTotalIncomeMap = new HashMap<>();
        for (Transaction incomeTransaction : incomeTransactions) {
            String category = incomeTransaction.getCategory();
            int amount = incomeTransaction.getAmount();
            categoryTotalIncomeMap.put(category, categoryTotalIncomeMap.getOrDefault(category, 0) + amount);
        }

        // categoryTotalExpensesMap contains the total amount for each expense category
        ArrayList<PieEntry> entriesExpense = new ArrayList<>();
        ArrayList<Integer> colorsExpense = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryTotalExpensesMap.entrySet()) {
            entriesExpense.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            Integer color = categoryColors.get(entry.getKey());
            if (color != null) {
                colorsExpense.add(color);
            } else {
                colorsExpense.add(Color.parseColor("#7FFF00"));
            }
            Log.d(TAG, "Expense Category: " + entry.getKey() + ", Total Amount: " + entry.getValue());
        }
        // categoryTotalIncomeMap contains the total amount for each income category
        ArrayList<PieEntry> entriesIncome = new ArrayList<>();
        ArrayList<Integer> colorsIncome = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryTotalIncomeMap.entrySet()) {
            entriesIncome.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            Integer color = categoryColors.get(entry.getKey());
            if (color != null) {
                colorsIncome.add(color);
            } else {
                colorsIncome.add(Color.parseColor("#7FFF00"));
            }
            Log.d(TAG, "Income Category: " + entry.getKey() + ", Total Amount: " + entry.getValue());
        }
        setupPieChart(expensesPieChart, entriesExpense, colorsExpense);
        setupPieChart(incomesPieChart, entriesIncome, colorsIncome);

        expenseReport.setOnClickListener(v -> startNewActivity(categoryTotalExpensesMap));

        incomesReport.setOnClickListener(v -> startNewActivity(categoryTotalIncomeMap));
    }

    private void startNewActivity(Map<String, Integer> categoryTotalMap) {
        if (categoryTotalMap != null && !categoryTotalMap.isEmpty()) {
            for (Map.Entry<String, Integer> entry : categoryTotalMap.entrySet()) {
                Log.d(TAG, "Category: " + entry.getKey() + ", Total: " + entry.getValue());
            }

            Intent intent = new Intent(getActivity(), ReportExpandData.class);
            intent.putExtra("categoryTotalMap", (Serializable) categoryTotalMap);
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "No data to show.", Toast.LENGTH_SHORT).show();
        }
    }


    private void setupPieChart(final PieChart pieChart, ArrayList<PieEntry> entries, ArrayList<Integer> colors) {
        if (entries == null || entries.isEmpty() || colors == null || colors.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No data available");
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawValues(false);

        if (!colors.isEmpty()) {
            dataSet.setColors(colors);
        } else {
            dataSet.setColors(Color.parseColor("#7FFF00"));
        }

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setRotationEnabled(false);
        pieChart.invalidate();

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e == null)
                    return;
                PieEntry entry = (PieEntry) e;
                float value = entry.getValue();
                String label = entry.getLabel();
                pieChart.setCenterText(label + ": " + value + "VND");
                pieChart.invalidate();
            }

            @Override
            public void onNothingSelected() {
                pieChart.setCenterText("");
            }
        });
    }

    private void setupBarChart(List<Transaction> transactions) {

        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        int totalIncome = 0;
        int totalExpense = 0;

        for (Transaction transaction : transactions) {
            if ("Income".equals(transaction.getType())) {
                totalIncome += transaction.getAmount();
            } else if ("Expense".equals(transaction.getType())) {
                totalExpense += transaction.getAmount();
            }
        }
        incomeEntries.add(new BarEntry(0, totalIncome));
        expenseEntries.add(new BarEntry(1, totalExpense));

        // Create BarDataSet for income and expense
        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "Khoản thu");
        incomeDataSet.setColor(Color.parseColor("#009AA6"));
        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Khoản chi");
        expenseDataSet.setColor(Color.parseColor("#FF4C4C"));

        // Create BarData and add the BarDataSets
        BarData barData = new BarData();
        barData.addDataSet(incomeDataSet);
        barData.addDataSet(expenseDataSet);

        // Set the BarData to the BarChart
        barChart.setData(barData);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

}
