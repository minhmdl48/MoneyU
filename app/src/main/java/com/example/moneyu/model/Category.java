package com.example.moneyu.model;

public class Category {
    private String categoryName;
    private String icon;
    public Category(String categoryName, String icon) {
        this.categoryName = categoryName;
        this.icon = icon;
    }

    // Getters and setters
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
