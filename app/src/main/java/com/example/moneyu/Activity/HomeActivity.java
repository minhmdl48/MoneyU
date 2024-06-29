package com.example.moneyu.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.moneyu.Activity.Login_Signup.LoginActivity;
import com.example.moneyu.Fragments.HomeFragment;

import com.example.moneyu.Fragments.ReportsFragment;

import com.example.moneyu.Fragments.RecordFragment;

import com.example.moneyu.R;
import com.example.moneyu.Transaction.AddTransactionActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    // UI Elements
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private BottomNavigationView bottomNavigationView;
    private TextView datePlaceholderText,leftPlaceholderText,rightPlaceholderText;
    private ImageView rightButton,leftButton,menuIcon;
    private Fragment defaultFragment;
    private boolean isFragmentTransactionPending = false;
    private long lastClickTime = 0;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private String userID,displayedFragment;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    // Other
    private long totalAmount = 0; // Variable to store total amount

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseAuth = FirebaseAuth.getInstance();

        // Check if user is authenticated
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You are not logged in. Redirecting to login page...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Get user ID and Firestore instance
        userID = firebaseAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // Calculate user financial details
        calculateTotalAmount();
//        calculateFutureTransactionsAmount();

        // Initialize UI components
        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        leftPlaceholderText = findViewById(R.id.left_placeholder_text);
//        rightPlaceholderText = findViewById(R.id.right_placeholder_text);
//        datePlaceholderText = findViewById(R.id.placeholder_text);
//        leftButton = findViewById(R.id.left_button);
//        rightButton = findViewById(R.id.right_button);

        // Set up date format and calendar
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MM-yyyy", Locale.getDefault());

        // Update date display and manage button visibility
//        updateDateDisplay();

        // Set up date navigation buttons
//        leftButton.setOnClickListener(v -> {
//            changeDate(-1);
//            loadCurrentFragment();
//        });
//
//        rightButton.setOnClickListener(v -> {
//            changeDate(1);
//            loadCurrentFragment();
//        });

        // Set default fragment to HomeFragment
        bottomNavigationView.setSelectedItemId(R.id.home);
        replaceFragment(new HomeFragment());

        // Set up navigation drawer toggle
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_nav_drawer, R.string.close_nav_drawer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(findViewById(R.id.nav_view)));

        // Set up navigation view listener
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView userEmailTextView = headerView.findViewById(R.id.textView_email);
        userEmailTextView.setText(firebaseAuth.getCurrentUser().getEmail());
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Set up bottom navigation view listener with debounce
        bottomNavigationView.setOnItemSelectedListener(item -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 500) {
                return false; // Prevent double-click within 500ms
            }
            lastClickTime = currentTime;

            return onBottomNavigationItemSelected(item);
        });

    }

    // Update the date display and manage right button visibility
//    private void updateDateDisplay() {
//        String currentDate = dateFormat.format(calendar.getTime());
//        datePlaceholderText.setText(currentDate);
//        Calendar today = Calendar.getInstance();
//        if (dateFormat.format(today.getTime()).equals(currentDate)) {
//            rightButton.setVisibility(View.GONE);
//            rightButton.setEnabled(false);
//        } else {
//            rightButton.setVisibility(View.VISIBLE);
//            rightButton.setEnabled(true);
//        }
//    }

    // Change the displayed date by a specified number of months
    private void changeDate(int months) {
        calendar.add(Calendar.MONTH, months);
//        updateDateDisplay();
    }

    // Load the currently displayed fragment
    private void loadCurrentFragment() {
        if (displayedFragment == null) {
            // Handle the case where displayedFragment is null
            replaceFragment(new HomeFragment());
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.executePendingTransactions();

        switch (displayedFragment) {
            case "home":
                replaceFragment(new HomeFragment());
                break;
//            case "operations":
//                replaceFragment(new ReviewFragment());
//                break;
//            case "summary":
//                replaceFragment(new SummaryFragment());
//                break;
            case "reports":
                replaceFragment(new ReportsFragment());
                break;
            default:
                replaceFragment(new HomeFragment());
                break;
        }
    }

    // Replace the current fragment
    private synchronized void replaceFragment(Fragment fragment) {
        if (isFragmentTransactionPending) {
            return; // Prevent overlapping transactions
        }
        isFragmentTransactionPending = true;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commitAllowingStateLoss();

        fragmentManager.executePendingTransactions();
        isFragmentTransactionPending = false;
    }

    // Handle bottom navigation item selection
    private boolean onBottomNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.home) {
            fragment = new HomeFragment();
//            setVisibility(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE);
            displayedFragment = "home";
        }else if(itemId == R.id.add_transaction){
            Intent intent = new Intent(HomeActivity.this, AddTransactionActivity.class);
            startActivity(intent);
            return true;
        }
        else if (itemId == R.id.operations) {
            fragment = new RecordFragment();
            displayedFragment = "operations";
        }
//        else if (itemId == R.id.add_transaction) {
//            fragment = new SummaryFragment();
//            setVisibility(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
//            displayedFragment = "summary";
         else if (itemId == R.id.report) {
            fragment = new ReportsFragment();
//            setVisibility(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.INVISIBLE);
            displayedFragment = "reports";
        }

        if (fragment != null) {
            calendar = Calendar.getInstance();
//            updateDateDisplay(); // Update date display to current date
            replaceFragment(fragment);
            return true;
        }
        return false;
    }

    // Set visibility of date-related views and floating button
//    private void setVisibility(int date, int left, int right, int floating) {
//        datePlaceholderText.setVisibility(date);
//        leftButton.setVisibility(left);
//        rightButton.setVisibility(right);
//    }

    // Handle navigation item selection
    private boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        if (itemId == R.id.home_item) {
            bottomNavigationView.setSelectedItemId(R.id.home);

//        } else if (itemId == R.id.setBudget_item) {
//            startActivity(new Intent(HomeActivity.this, SetBudgetsActivity.class));
//        } else if (itemId == R.id.recurringPayments_item) {
//            startActivity(new Intent(HomeActivity.this, RecurringPaymentsActivity.class));
//        } else if (itemId == R.id.plannedTransactions_item) {
//            startActivity(new Intent(HomeActivity.this, PlannedTransactionsActivity.class));
//        } else if (itemId == R.id.about_item) {
//            startActivity(new Intent(HomeActivity.this, AboutActivity.class));
        } else if (itemId == R.id.logout_item) {
            showLogoutPrompt();
        }


        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Show logout prompt
    private void showLogoutPrompt() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    finishAffinity();
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void calculateTotalAmount() {
        // Get today's date
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String todayString = dateFormat.format(today);

        db.collection("transactions")
                .whereEqualTo("userId", userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        long totalExpense = 0;
                        long totalIncome = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {

                                String type = document.getString("type"); // Assuming "type" field indicates Expense or Income
                                String dateString = document.getString("date"); // Assuming "date" field stores the transaction date

                                // Parse the transaction date
                                Date transactionDate = dateFormat.parse(dateString);

                                Long amount1 = document.getLong("amount");
                                if (amount1 != null) {
                                    if (type.equals("Expense")) {
                                        totalExpense -= amount1;
                                    } else if (type.equals("Income")) {
                                        totalIncome += amount1;
                                    }
                                }
                            } catch (NumberFormatException | ParseException e) {
                                // Handle the case where parsing fails or the amount is not a valid number
                                Toast.makeText(HomeActivity.this, "Invalid transaction data", Toast.LENGTH_SHORT).show();
                            }
                        }
                        totalAmount = totalIncome + totalExpense;

                        // Format the total amount string to display only three numbers after the decimal point
                        String totalAmountText = String.format(Locale.getDefault(), "Số dư: %dđ", totalAmount);
                        leftPlaceholderText.setText(totalAmountText);
                    } else {
                        Toast.makeText(HomeActivity.this, "Failed to retrieve transactions", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set the default fragment to TransactionsFragment
        defaultFragment = new HomeFragment();
        replaceFragment(defaultFragment);
        // Set the selected item in the bottom navigation view to Home
        bottomNavigationView.setSelectedItemId(R.id.home);

        // Recalculate amounts in case of any changes while the activity was paused
        calculateTotalAmount();
//        calculateFutureTransactionsAmount();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new MaterialAlertDialogBuilder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit the app? You will also be logged out.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Log out from the Firebase authentication account
                    FirebaseAuth.getInstance().signOut();
                    // Close all activities
                    finishAffinity();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

//    public String getCurrentDate() {
//        return datePlaceholderText.getText().toString(); // Assuming placeholderText is a TextView
//    }

}
