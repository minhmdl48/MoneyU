package com.example.moneyu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneyu.Transaction.DetailTransactionActivity;
import com.example.moneyu.model.Transaction;
import com.example.moneyu.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {

    private final Context context;
    private List<Transaction> transactionList;
    private Map<String, Integer> categoryColorMap;

    public HomeAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
        initializeCategoryColorMap();
    }

    // Method to set the transaction list
    public void setTransactions(List<Transaction> transactions) {
        int initialSize = this.transactionList.size();
        this.transactionList = transactions;
        for (int i = initialSize; i < transactions.size(); i++) {
            notifyItemInserted(i);
        }
    }

    // Initialize the category-color mapping
    private void initializeCategoryColorMap() {
        categoryColorMap = new HashMap<>();
        categoryColorMap.put("Uncategorized", R.color.green);
        categoryColorMap.put("Food", R.color.pink);
        categoryColorMap.put("Entertainment", R.color.green);
        categoryColorMap.put("Transportation", R.color.blue);
        categoryColorMap.put("Home", R.color.brown);
        categoryColorMap.put("Clothing", R.color.purple);
        categoryColorMap.put("Car", R.color.navy);
        categoryColorMap.put("Electronics", R.color.orange);
        categoryColorMap.put("Health and Beauty", R.color.red);
        categoryColorMap.put("Education", R.color.cyan);
        categoryColorMap.put("Children", R.color.yellow);
        categoryColorMap.put("Work", R.color.gray);
        categoryColorMap.put("Bureaucracy", R.color.maroon);
        categoryColorMap.put("Gifts", R.color.deep_gold);
        categoryColorMap.put("Bank", R.color.black);
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new HomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }
    public class HomeViewHolder extends RecyclerView.ViewHolder {

        private TextView categoryTextView;
        private TextView amountTextView;
        private TextView dateTextView;
        private TextView itemColorTextView;
        private View itemColorCircle;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            itemColorTextView = itemView.findViewById(R.id.itemColorTextView);
            itemColorCircle = itemView.findViewById(R.id.itemColorCircle);

            itemView.setOnClickListener(v->{
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Transaction clickedTransaction = transactionList.get(position);
                    Intent intent = new Intent(context, DetailTransactionActivity.class);
                    intent.putExtra("transactionId", clickedTransaction.getTransactionId());
                    context.startActivity(intent);
                }
            });

        }

        public void bind(Transaction transaction) {
            // Set the first letter of the category to itemColorTextView
            String category = transaction.getCategory();
            categoryTextView.setText(transaction.getCategory());
            dateTextView.setText(transaction.getDate());
            if (!category.isEmpty()) {
                char firstLetter = category.charAt(0);
                itemColorTextView.setText(String.valueOf(firstLetter).toUpperCase()); // Convert to uppercase if needed
            } else {
                itemColorTextView.setText("U"); // Default to "U" if category is empty
            }

            // Set the background color of itemColorCircle based on the category
            Integer colorResId = categoryColorMap.getOrDefault(category, R.color.black);
            itemColorCircle.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, colorResId)));

            // Set the amount text
            String amount = String.valueOf(transaction.getAmount());

            Log.d("HomeAdapter", "Amount: " + amount);

            if (transaction.getType().equals("Expense")) {
                amountTextView.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                amount = "-" + amount; // Prefix "-" for expenses
            } else if (transaction.getType().equals("Income")) {
                amountTextView.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                amount = "+" + amount; // Prefix "+" for income
            }
            amountTextView.setText(amount + "đ"); // Append € symbol after amount

            dateTextView.setText(transaction.getDate());
        }
    }
}

