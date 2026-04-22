package com.example.homepurchases.models;

public class Purchase {

    private int id;
    private String itemName;
    private int categoryId;
    private double price;
    private int quantity;
    private double totalCost;
    private long date;
    private String notes;

    public Purchase() {}

    public Purchase(int id, String itemName, int categoryId, double price,
                    int quantity, double totalCost, long date, String notes) {
        this.id = id;
        this.itemName = itemName;
        this.categoryId = categoryId;
        this.price = price;
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.date = date;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
