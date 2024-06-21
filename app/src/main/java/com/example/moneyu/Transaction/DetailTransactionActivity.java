package com.example.moneyu.Transaction;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moneyu.R;
import com.example.moneyu.model.Transaction;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailTransactionActivity extends AppCompatActivity {

    private TextView amountTextView, categoryTextView, dateTextView, notesTextView;
    private FirebaseFirestore db;
    private static final String TAG = "DetailTransactionActivity";
    private static final int CATEGORY_REQUEST_CODE = 10;
    private final String type = "Expense";
    private final String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        amountTextView = findViewById(R.id.amount);
        categoryTextView = findViewById(R.id.category_text_view);
        dateTextView = findViewById(R.id.date_picker_text_view);
        notesTextView = findViewById(R.id.notes_text_view);
        ImageView closeIcon = findViewById(R.id.close_icon);
        ImageView deleteIcon = findViewById(R.id.delete_icon);
        Button btnSave = findViewById(R.id.save_button);

        db = FirebaseFirestore.getInstance();

        String transactionId = getIntent().getStringExtra("transactionId");
        Log.d("DetailTransactionActivity", "Transaction ID: " + transactionId);

        if (transactionId != null) {
            fetchTransaction(transactionId);
        } else {
            Toast.makeText(this, "No transaction ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        dateTextView.setOnClickListener(v -> showMaterialPicker(dateTextView, "Select transaction date"));

        categoryTextView.setOnClickListener(v -> {
            Intent intent = new Intent(DetailTransactionActivity.this, SelectCategoryActivity.class);
            startActivityForResult(intent, CATEGORY_REQUEST_CODE);
        });

        closeIcon.setOnClickListener(v -> finish());

        notesTextView.setOnClickListener(v -> showCustomDialog("Insert note", notesTextView));

        btnSave.setOnClickListener(v -> {
            String amount = amountTextView.getText().toString().trim();
            Log.d("DetailTransactionActivity", "Amount: " + amount);
            if (amount.isEmpty()) {
                showErrorDialog("Error Occurred", "Amount cannot be empty");
            } else if (amount.equals("0")) {
                showErrorDialog("Error Occurred", "Amount cannot be equal to 0");
            } else {
                Transaction transaction = new Transaction();
                transaction.setUserId(userId);
                transaction.setTransactionId(transactionId);
                try {
                    int amount1 = Integer.parseInt(amount);
                    transaction.setAmount(amount1);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing amount: " + e.getMessage(), e);
                }
                transaction.setCategory(categoryTextView.getText().toString());
                transaction.setType(type);
                transaction.setDate(dateTextView.getText().toString());
                transaction.setNote(notesTextView.getText().toString());

                updateTransaction(transactionId, transaction);
            }
        });

        deleteIcon.setOnClickListener(v -> new MaterialAlertDialogBuilder(DetailTransactionActivity.this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteTransaction(transactionId);
                })
                .setNegativeButton("Cancel", null)
                .show());
    }

    public void updateTransaction(String transactionId, Transaction updatedTransaction) {
        db.collection("transactions")
                .whereEqualTo("transactionId", transactionId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().update(
                                            "amount", updatedTransaction.getAmount(),
                                            "category", updatedTransaction.getCategory(),
                                            "date", updatedTransaction.getDate(),
                                            "note", updatedTransaction.getNote(),
                                            "type", updatedTransaction.getType()
                                    )
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Transaction updated successfully"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error updating transaction: ", e));
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    // Delete transaction from Firestore
    public void deleteTransaction(String transactionId) {
        db.collection("transactions")
                .whereEqualTo("transactionId", transactionId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Delete each document
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Delete successfully", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "Transaction deleted successfully");
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error deleting transaction: " + e.getMessage(), e);
                                    });
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CATEGORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            assert data != null;
            String selectedCategory = data.getStringExtra("selected_category");
            categoryTextView.setText(selectedCategory);
        }
    }

    private void fetchTransaction(String transactionId) {
        db.collection("transactions")
                .whereEqualTo("transactionId", transactionId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                Transaction transaction = document.toObject(Transaction.class);
                                amountTextView.setText(String.valueOf(transaction.getAmount()));
                                categoryTextView.setText(transaction.getCategory());
                                dateTextView.setText(transaction.getDate());
                                notesTextView.setText(transaction.getNote());
                                return;
                            }
                        }

                        Toast.makeText(DetailTransactionActivity.this, "Transaction not found", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Log.d(TAG, "Transaction with ID " + transactionId + " does not exist.");
                        Toast.makeText(DetailTransactionActivity.this, "Transaction not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void showMaterialPicker(final TextView targetTextView, String pickerTitle) {
        if (getSupportFragmentManager().findFragmentByTag("MATERIAL_DATE_PICKER") == null) {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText(pickerTitle);

            builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

            MaterialDatePicker<Long> materialDatePicker = builder.build();
            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                String dateString = sdf.format(new Date(selection));

                targetTextView.setText(dateString);
            });
            materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        }
    }

    private void showCustomDialog(String title, final TextView textView) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_text_input);

        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        final EditText dialogInput = dialog.findViewById(R.id.dialog_input);
        TextView dialogOk = dialog.findViewById(R.id.dialog_ok);
        TextView dialogCancel = dialog.findViewById(R.id.dialog_cancel);

        dialogTitle.setText(title);

        dialogOk.setOnClickListener(v -> {
            String inputText = dialogInput.getText().toString().trim();
            textView.setText(inputText);
            dialog.dismiss();
        });

        dialogCancel.setOnClickListener(v -> dialog.dismiss());

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog.show();
    }

    private void showErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
