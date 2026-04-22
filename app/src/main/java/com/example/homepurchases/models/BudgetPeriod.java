package com.example.homepurchases.models;

public enum BudgetPeriod {
    DAILY(0),
    WEEKLY(1),
    MONTHLY(2);

    private final int value;

    BudgetPeriod(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static BudgetPeriod fromValue(int value) {
        for (BudgetPeriod period : values()) {
            if (period.value == value) return period;
        }
        return MONTHLY;
    }
}
