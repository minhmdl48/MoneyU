package com.example.moneyu.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneyu.R;
import com.example.moneyu.model.Transaction;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();
        recyclerView = rootView.findViewById(R.id.parentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;
        return null;
    }

    private void retrieveTransactions(String userId) {
        // Retrieve transactions
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                // Retrieve transactions
                                List<Transaction> transactionList = task.getResult().toObjects(Transaction.class);
                                // Sort transactions by date
                                Collections.sort(transactionList, new Comparator<Transaction>() {
                                    @Override
                                    public int compare(Transaction o1, Transaction o2) {
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                        try {
                                            Date date1 = sdf.parse(o1.getDate());
                                            Date date2 = sdf.parse(o2.getDate());
                                            return date2.compareTo(date1);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        return 0;
                                    }
                                });
                                // Update the adapter with the sorted transactions
                                categoryAdapter.updateTransactions(transactionList);
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                );
                )
    }

}
