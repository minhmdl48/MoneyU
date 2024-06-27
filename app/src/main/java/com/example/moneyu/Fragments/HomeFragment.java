package com.example.moneyu.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneyu.Adapter.HomeAdapter;
import com.example.moneyu.R;
import com.example.moneyu.Models.Transaction;
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

public class HomeFragment extends Fragment {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private TextView txtViewSeeAll;
    private HomeAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();

        recyclerView = rootView.findViewById(R.id.parentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        txtViewSeeAll = rootView.findViewById(R.id.textViewSeeAll);

        adapter = new HomeAdapter(getContext(), transactionList);
        recyclerView.setAdapter(adapter);
        txtViewSeeAll.setOnClickListener(v -> {
            RecordFragment recordFragment = new RecordFragment();
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.drawer_layout, recordFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;

        if (userId != null) {
            retrieveTransactions(userId);
        } else {
            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    // Retrieve Transactions
    private void retrieveTransactions(String userId) {

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
                            transactionList.add(transaction);

                        }

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
                        if (transactionList.size() > 5) {
                            transactionList = transactionList.subList(0, 5);
                        }
                        adapter.setTransactions(transactionList);
                    } else {
                        Log.d("HomeFragment", "Error getting documents: ", task.getException());
                    }
                });
    }
}
