package com.example.moneyu.Fragments;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.moneyu.Adapter.HomeAdapter;
import com.example.moneyu.R;
import com.example.moneyu.model.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordFragment extends Fragment {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private TextView txtDateLabel,txtDatePicker, txtIncome,txtIncomeDisplay,txtOutcome,txtOutcomeDisplay;
    private HomeAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();
    public RecordFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_record, container, false);

        db = FirebaseFirestore.getInstance();
        txtDateLabel = rootView.findViewById(R.id.txtDateLabel);
        txtDatePicker = rootView.findViewById(R.id.txtDatePicker);
        txtIncome = rootView.findViewById(R.id.txtIncome);
        txtIncomeDisplay = rootView.findViewById(R.id.txtIncomeDisplay);
        txtOutcome = rootView.findViewById(R.id.txtOutcome);
        txtOutcomeDisplay = rootView.findViewById(R.id.txtOutcomeDisplay);
        recyclerView = rootView.findViewById(R.id.parentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        txtIncomeDisplay.setText("0");
        adapter = new HomeAdapter(getContext(), transactionList);
        recyclerView.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;

        if (userId != null) {
            String currentMonth = "06";
            String currentYear = "2024";

            retrieveTransactions(currentMonth, currentYear, userId);
            Log.d("HomeFragment", "");
        } else {
            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    // Retrieve Transactions
    private void retrieveTransactions(String currentMonth, String currentYear, String userId) {

        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        Date todayDate = todayCal.getTime();

        // Query the "transactions" collection in the database for transactions with the specified userId
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        transactionList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Transaction transaction = document.toObject(Transaction.class);
                            Long amount = document.getLong("amount");
                            if (amount != null) {
                                assert transaction != null;
                                transaction.setAmount(amount.intValue());
                            }
                            assert transaction != null;
                            String transactionDate = transaction.getDate();
                            Log.d("HomeFragment", "Transaction Date: " + transactionDate);

                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            transactionList.add(transaction);
                        }
                        // Sort the transactionList by date in reverse order
                        transactionList.sort((t1, t2) -> {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
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
                        Log.d("HomeFragment", "sizee: " + String.valueOf(transactionList.size()));

                        adapter.setTransactions(transactionList);
                    } else {
                        Log.d("HomeFragment", "Error getting documents: ", task.getException());
                    }
                });
    }
}

