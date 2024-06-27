package com.example.moneyu.Fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneyu.Adapter.HomeAdapter;
import com.example.moneyu.Models.Transaction;
import com.example.moneyu.R;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordFragment extends Fragment {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private TextView txtDatePicker, txtIncomeDisplay, txtOutcomeDisplay;
    private ImageView btnDatePicker;
    private HomeAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private Date startDate, endDate;

    public RecordFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_record, container, false);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;

        txtDatePicker = rootView.findViewById(R.id.txtDatePicker);
        txtIncomeDisplay = rootView.findViewById(R.id.txtIncomeDisplay);
        txtOutcomeDisplay = rootView.findViewById(R.id.txtOutcomeDisplay);
        btnDatePicker = rootView.findViewById(R.id.btnDatePicker);
        recyclerView = rootView.findViewById(R.id.parentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        MaterialDatePicker.Builder<Pair<Long, Long>> materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker();
        materialDateBuilder.setTitleText("SELECT A DATE");

        final MaterialDatePicker<Pair<Long, Long>> materialDatePicker = materialDateBuilder.build();
        btnDatePicker.setOnClickListener(
                v -> {
                    materialDatePicker.show(requireActivity().getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
                });

        materialDatePicker.addOnPositiveButtonClickListener(
                new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onPositiveButtonClick(Pair<Long, Long> selection) {
                        if (selection == null) {
                            // The user hasn't selected a date range yet
                            Toast.makeText(getContext(), "Please select a date range", Toast.LENGTH_SHORT).show();
                        } else {
                            // The user has selected a date range
                            Long startDateLong = selection.first;
                            Long endDateLong = selection.second;

                            // Convert the Long values to Date objects
                            startDate = new Date(startDateLong);
                            endDate = new Date(endDateLong);

                            // Create a SimpleDateFormat object with the desired format
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                            // Format the Date objects into the desired format
                            String formattedStartDate = sdf.format(startDate);
                            String formattedEndDate = sdf.format(endDate);

                            // Set the formatted dates in the TextView
                            txtDatePicker.setText(formattedStartDate + " - " + formattedEndDate);
                            calculateMonthlyIncome(startDate, endDate, userId);
                            calculateMonthlyExpense(startDate, endDate, userId);
                            retrieveTransactions(startDate, endDate, userId);
                        }
                    }
                });
        adapter = new HomeAdapter(getContext(), transactionList);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    // Retrieve Transactions
    private void retrieveTransactions(Date startDate, Date endDate, String userId) {
        // Query the "transactions" collection in the database for transactions with the specified userId
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        transactionList = new ArrayList<>();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        for (DocumentSnapshot document : task.getResult()) {
                            Transaction transaction = document.toObject(Transaction.class);
                            String transactionDateString = document.getString("date");
                            try {
                                assert transactionDateString != null;
                                Date transactionDate = dateFormat.parse(transactionDateString);
                                if (transactionDate != null && startDate != null && endDate != null && !transactionDate.before(startDate) && !transactionDate.after(endDate)) {
                                    Long amount = document.getLong("amount");
                                    if (amount != null) {
                                        assert transaction != null;
                                        transaction.setAmount(amount.intValue());
                                    }
                                    assert transaction != null;
                                    transactionList.add(transaction);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        // Sort the transactionList by date in reverse order
                        transactionList.sort((t1, t2) -> {
                            try {
                                Date date1 = dateFormat.parse(t1.getDate());
                                Date date2 = dateFormat.parse(t2.getDate());

                                assert date2 != null;
                                return date2.compareTo(date1);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return 0;
                            }
                        });
                        adapter.setTransactions(transactionList);
                    } else {
                        Log.d("RecordFragment", "Error getting documents: ", task.getException());
                    }
                });
    }


    private void calculateMonthlyIncome(Date startDate, Date endDate, String userId) {
        // Query the "transactions" collection in the database for transactions with the specified userId and type "Income"
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", "Income")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalIncome = 0;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        for (DocumentSnapshot document : task.getResult()) {
                            String transactionDateString = document.getString("date");
                            try {
                                assert transactionDateString != null;
                                Date transactionDate = dateFormat.parse(transactionDateString);

                                if (transactionDate != null && startDate != null && endDate != null && !transactionDate.before(startDate) && !transactionDate.after(endDate)) {
                                    Long amount = document.getLong("amount");
                                    if (amount != null) {
                                        totalIncome += amount.intValue();
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        // Set the total income in the txtIncomeDisplay TextView
                        txtIncomeDisplay.setText(String.valueOf(totalIncome));
                    } else {
                        Log.d("RecordFragment", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void calculateMonthlyExpense(Date startDate, Date endDate, String userId) {
        // Query the "transactions" collection in the database for transactions with the specified userId and type "Expense"
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", "Expense")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalExpense = 0;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        for (DocumentSnapshot document : task.getResult()) {
                            String transactionDateString = document.getString("date");
                            try {
                                assert transactionDateString != null;
                                Date transactionDate = dateFormat.parse(transactionDateString);
                                if (transactionDate != null && startDate != null && endDate != null && !transactionDate.before(startDate) && !transactionDate.after(endDate)) {
                                    Long amount = document.getLong("amount");
                                    if (amount != null) {
                                        totalExpense += amount.intValue();
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        // Set the total expense in the txtOutcomeDisplay TextView
                        txtOutcomeDisplay.setText(String.valueOf(totalExpense));
                    } else {
                        Log.d("RecordFragment", "Error getting documents: ", task.getException());
                    }
                });
    }

}

