package com.example.moneyu.Transaction;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moneyu.R;
import com.example.moneyu.model.Transaction;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AddTransactionActivity extends AppCompatActivity {
    private String type = "Expense";
    private EditText amountText;
    private TextView notesTextView, datePickerTextView, categoryTextView;
    private ImageView backIcon;
    Button okTransactionButton;
    private MaterialDatePicker<Long> materialDatePicker;
    private static final int CATEGORY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        String userId = getIntent().getStringExtra("USER_ID");

        backIcon = findViewById(R.id.back_icon);
        amountText = findViewById(R.id.amount);
        notesTextView = findViewById(R.id.notes_text_view);
        datePickerTextView = findViewById(R.id.date_picker_text_view);
        okTransactionButton = findViewById(R.id.ok_transacion_button);
        categoryTextView = findViewById(R.id.category_text_view);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        datePickerTextView.setText(currentDate);
        datePickerTextView.setOnClickListener(v -> showMaterialPicker(datePickerTextView, "Select transaction date"));

        categoryTextView.setOnClickListener(v -> {
            // Start the new activity to select category
            Intent intent = new Intent(AddTransactionActivity.this, SelectCategoryActivity.class);
//            startActivity(intent);
            startActivityForResult(intent, CATEGORY_REQUEST_CODE);
        });
//        String selectedCategory = getIntent().getStringExtra("selected_category");
//        Log.d("AddTransactionActivity", "Selected category: " + selectedCategory);
//        categoryTextView.setText(selectedCategory);

        amountText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amountText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (amountText.getText().toString().equals("0")) {
                    amountText.setText("");
                }
            }
        });

        backIcon.setOnClickListener(v -> finish());

        notesTextView.setOnClickListener(v -> showCustomDialog("Insert note", notesTextView));

        okTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = amountText.getText().toString().trim();
                String category = categoryTextView.getText().toString().isEmpty() ? "Uncategorized" : categoryTextView.getText().toString();
                if (amount.isEmpty()) {
                    // Show error dialog if amount is empty
                    showErrorDialog("Error Occurred", "Amount cannot be empty");
                } else if (amount.equals("0")) {
                    // Show error dialog if amount is zero
                    showErrorDialog("Error Occurred", "Amount cannot be equal to 0");
                }   else {

                        // If not Recurring, add the transaction normally
                        // Construct a Transaction object as before
                        Transaction transaction = new Transaction();
                        transaction.setUserId(userId);
                        transaction.setTransactionId(generateTransactionId());
                        transaction.setAmount(Integer.parseInt(amountText.getText().toString()));
                        transaction.setCategory(category);
                        transaction.setType(type);
                        transaction.setDate(datePickerTextView.getText().toString());
                        transaction.setNote(notesTextView.getText().toString());

                        Transaction transaction1 = new Transaction();
                        transaction1.setUserId("Z8k1Fnfr2yPAlcxg80DMa8KhArc2");
                        transaction1.setTransactionId("20");
                        transaction1.setAmount(2000);
                        transaction1.setCategory("Food");
                        transaction1.setType("Expense");
                        transaction1.setDate("20-10-2021");
                        transaction1.setNote("Lunch");

                        // Add transaction to Firestore database
                        db.collection("transactions")
                                .add(transaction1)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        // Transaction added successfully
                                        Toast.makeText(AddTransactionActivity.this, "Transaction added successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle errors
                                        Log.e("AddTransactionActivity", "Failed to add transaction: " + e.getMessage(), e);
                                        Toast.makeText(AddTransactionActivity.this, "Failed to add transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }

            // Method to generate a unique transaction ID
            private String generateTransactionId() {
                // Generate a unique transaction ID using a combination of timestamp and a random number
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
                String timestamp = sdf.format(new Date());
                Random random = new Random();
                int randomNumber = random.nextInt(10000); // Generate a random number between 0 and 9999
                return timestamp + randomNumber;
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CATEGORY_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String selectedCategory = data.getStringExtra("selected_category");
                categoryTextView.setText(selectedCategory);
                // Do something with the selected category
            }
        }
    }
    private void showErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
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

        dialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = dialogInput.getText().toString().trim();
                textView.setText(inputText);
                dialog.dismiss();
            }
        });

        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog.show();
    }

    private void showMaterialPicker(final TextView targetTextView, String pickerTitle) {
        if (getSupportFragmentManager().findFragmentByTag("MATERIAL_DATE_PICKER") == null) {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText(pickerTitle);

            // Set today's date as the default selection
            builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

            materialDatePicker = builder.build();
            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                // Convert the selected date to a readable format
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                String dateString = sdf.format(new Date(selection));

                targetTextView.setText(dateString);
            });
            materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        }
    }
}
