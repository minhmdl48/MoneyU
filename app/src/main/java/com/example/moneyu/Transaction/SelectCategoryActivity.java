package com.example.moneyu.Transaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneyu.Adapter.CategoryAdapter;
import com.example.moneyu.Models.Category;
import com.example.moneyu.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class SelectCategoryActivity extends AppCompatActivity {

    private RecyclerView parentRecyclerView;
    private List<Category> categories;
    private int[] circleColors;
    private ImageView backIcon;
    private String selectedTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        backIcon = findViewById(R.id.back_icon);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        RecyclerView recyclerView = findViewById(R.id.parentRecyclerView);


        parentRecyclerView = findViewById(R.id.parentRecyclerView);
        parentRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        circleColors = getResources().getIntArray(R.array.circle_colors);


        selectedTab = "Income";
        loadIncomeData(); // Load income data initially


        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // đóng activity và trở về màn hình trước đó
                finish();
            }
        });


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    selectedTab = "Income";
                    // Load Income data
                    loadIncomeData();
                } else if (position == 1) {
                    selectedTab = "Expense";
                    // Load Expense data
                    loadExpenseData();
                }
            }

            public void onTabUnselected(TabLayout.Tab tab) {
                // Handle tab unselected if necessary
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Handle tab reselected if necessary
            }
        });
    }

    private void loadIncomeData() {
        categories = new ArrayList<>();
        categories.add(new Category("Lương và Phụ cấp"));
        categories.add(new Category("Kinh doanh"));
        categories.add(new Category("Đầu tư"));
        categories.add(new Category("Quà tặng"));
        categories.add(new Category("Khác"));

        updateRecyclerView();
    }


    private void loadExpenseData() {
        categories = new ArrayList<>();
        categories.add(new Category("Đồ ăn"));
        categories.add(new Category("Di chuyển"));
        categories.add(new Category("Giải trí"));
        categories.add(new Category("Sức khoẻ và làm đẹp"));
        categories.add(new Category("Thuốc thang"));
        categories.add(new Category("Bảo hiểm"));
        categories.add(new Category("Mua sắm"));
        categories.add(new Category("Du lịch"));
        categories.add(new Category("Khác"));

        updateRecyclerView();
    }


    private void updateRecyclerView() {
        CategoryAdapter categoryAdapter = new CategoryAdapter(this, categories);
        parentRecyclerView.setAdapter(categoryAdapter);


        categoryAdapter.setOnItemClickListener(new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                Category clickedCategory = categories.get(position);


                String selectedCategory = clickedCategory.getCategoryName();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_category", selectedCategory);
                resultIntent.putExtra("selected_tab", selectedTab);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
