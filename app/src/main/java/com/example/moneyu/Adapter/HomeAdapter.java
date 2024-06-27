package com.example.moneyu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    public HomeAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    // Method to set the transaction list
    public void setTransactions(List<Transaction> transactions) {
        int initialSize = this.transactionList.size();
        this.transactionList = transactions;
        for (int i = initialSize; i < transactions.size(); i++) {
            notifyItemInserted(i);
        }
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
        private ImageView categoryIconImageView;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            categoryIconImageView = itemView.findViewById(R.id.categoryIconImageView);
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
            switch (transaction.getCategory()) {
                case "Lương":
                    categoryIconImageView.setImageResource(R.drawable.ic_salary);
                    break;
                case "Bán đồ":
                    categoryIconImageView.setImageResource(R.drawable.ic_sale);
                    break;
                case "Tiền lãi":
                    categoryIconImageView.setImageResource(R.drawable.ic_interest);
                    break;
                case "Thu nhập khác":
                    categoryIconImageView.setImageResource(R.drawable.ic_others);
                    break;
                case "Ăn uống":
                    categoryIconImageView.setImageResource(R.drawable.ic_food);
                    break;
                case "Di chuyển":
                    categoryIconImageView.setImageResource(R.drawable.ic_transportation);
                    break;
                case "Bảo hiểm":
                    categoryIconImageView.setImageResource(R.drawable.ic_insurance);
                    break;
                case "Sức khỏe":
                    categoryIconImageView.setImageResource(R.drawable.ic_healthcare);
                    break;
                case "Mua sắm":
                    categoryIconImageView.setImageResource(R.drawable.ic_shopping);
                    break;
                case "Du lịch":
                    categoryIconImageView.setImageResource(R.drawable.ic_travel);
                    break;
                case "Giáo dục":
                    categoryIconImageView.setImageResource(R.drawable.ic_education);
                    break;
                case "Hóa đơn":
                    categoryIconImageView.setImageResource(R.drawable.ic_bill);
                    break;
                default:
                    categoryIconImageView.setImageResource(R.drawable.ic_others);
                    break;
            }

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

