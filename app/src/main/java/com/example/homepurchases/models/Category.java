package com.example.homepurchases.models;

public class Category {

    private int id;
    private String name;
    private String iconName;
    private String description;

    public Category() {}

    public Category(int id, String name, String iconName, String description) {
        this.id = id;
        this.name = name;
        this.iconName = iconName;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return name;
    }
}
