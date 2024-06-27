package com.example.moneyu.Models;

public class Category {
    private String categoryName;
    public Category(String categoryName) {
        this.categoryName = categoryName;
    }

    // Getters and setters
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
