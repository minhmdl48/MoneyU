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
import com.example.moneyu.Fragments.RecordFragment;
import com.example.moneyu.Fragments.ReportsFragment;
import com.example.moneyu.R;
import com.example.moneyu.Transaction.AddTransactionActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    // UI Elements
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private BottomNavigationView bottomNavigationView;
    private TextView leftPlaceholderText;
    private ImageView menuIcon;
    private Fragment defaultFragment;
    private boolean isFragmentTransactionPending = false;
    private long lastClickTime = 0;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private String userID;

    // Other
    private long totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You are not logged in. Redirecting to login page...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return;
        }

        userID = firebaseAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        calculateTotalAmount();

        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        leftPlaceholderText = findViewById(R.id.left_placeholder_text);

        bottomNavigationView.setSelectedItemId(R.id.home);
        replaceFragment(new HomeFragment());

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_nav_drawer, R.string.close_nav_drawer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(findViewById(R.id.nav_view)));

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView userEmailTextView = headerView.findViewById(R.id.textView_email);
        userEmailTextView.setText(firebaseAuth.getCurrentUser().getEmail());
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 500) {
                return false;
            }
            lastClickTime = currentTime;

            return onBottomNavigationItemSelected(item);
        });

    }

    private synchronized void replaceFragment(Fragment fragment) {
        if (isFragmentTransactionPending) {
            return;
        }
        isFragmentTransactionPending = true;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commitAllowingStateLoss();

        fragmentManager.executePendingTransactions();
        isFragmentTransactionPending = false;
    }

    private boolean onBottomNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.add_transaction) {
            Intent intent = new Intent(HomeActivity.this, AddTransactionActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.operations) {
            fragment = new RecordFragment();
        } else if (itemId == R.id.report) {
            fragment = new ReportsFragment();
        }

        if (fragment != null) {
            replaceFragment(fragment);
            return true;
        }
        return false;
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        if (itemId == R.id.home_item) {
            bottomNavigationView.setSelectedItemId(R.id.home);
        } else if (itemId == R.id.logout_item) {
            showLogoutPrompt();
        }


        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

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
        db.collection("transactions")
                .whereEqualTo("userId", userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        long totalExpense = 0;
                        long totalIncome = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String type = document.getString("type");
                                Long amount1 = document.getLong("amount");
                                if (amount1 != null) {
                                    assert type != null;
                                    if (type.equals("Expense")) {
                                        totalExpense -= amount1;
                                    } else if (type.equals("Income")) {
                                        totalIncome += amount1;
                                    }
                                }
                            } catch (NumberFormatException e) {
                                Toast.makeText(HomeActivity.this, "Invalid transaction data", Toast.LENGTH_SHORT).show();
                            }
                        }
                        totalAmount = totalIncome + totalExpense;

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
        defaultFragment = new HomeFragment();
        replaceFragment(defaultFragment);
        bottomNavigationView.setSelectedItemId(R.id.home);

        calculateTotalAmount();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new MaterialAlertDialogBuilder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit the app? You will also be logged out.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    finishAffinity();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
