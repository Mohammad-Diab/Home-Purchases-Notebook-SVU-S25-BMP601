package com.example.homepurchases.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private static final String PREFS_NAME = "HomePurchasesPrefs";

    private static final String KEY_THEME_MODE    = "theme_mode";
    private static final String KEY_THEME_ACCENT  = "theme_accent";
    private static final String KEY_CURRENCY_TYPE = "currency_type";
    private static final String KEY_BUDGET_AMOUNT = "budget_amount";
    private static final String KEY_BUDGET_PERIOD = "budget_period";
    private static final String KEY_BUDGET_RESET_DAY = "budget_reset_day";

    public static final int MODE_LIGHT  = 0;
    public static final int MODE_DARK   = 1;

    public static final int CURRENCY_NEW = 0;  // ل.س جديدة
    public static final int CURRENCY_OLD = 1;  // ل.س قديمة

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static int getThemeMode(Context context) {
        return prefs(context).getInt(KEY_THEME_MODE, MODE_LIGHT);
    }

    public static void saveThemeMode(Context context, int mode) {
        prefs(context).edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public static int getThemeAccent(Context context) {
        return prefs(context).getInt(KEY_THEME_ACCENT, 0);
    }

    public static void saveThemeAccent(Context context, int accent) {
        prefs(context).edit().putInt(KEY_THEME_ACCENT, accent).apply();
    }

    public static int getCurrencyType(Context context) {
        return prefs(context).getInt(KEY_CURRENCY_TYPE, CURRENCY_NEW);
    }

    public static void saveCurrencyType(Context context, int type) {
        prefs(context).edit().putInt(KEY_CURRENCY_TYPE, type).apply();
    }

    public static float getBudgetAmount(Context context) {
        return prefs(context).getFloat(KEY_BUDGET_AMOUNT, 0f);
    }

    public static void saveBudgetAmount(Context context, float amount) {
        prefs(context).edit().putFloat(KEY_BUDGET_AMOUNT, amount).apply();
    }

    public static int getBudgetPeriod(Context context) {
        return prefs(context).getInt(KEY_BUDGET_PERIOD, 2); // default: MONTHLY
    }

    public static void saveBudgetPeriod(Context context, int period) {
        prefs(context).edit().putInt(KEY_BUDGET_PERIOD, period).apply();
    }

    // Weekly: 0=Sat, 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri
    // Monthly: 1–31
    public static int getBudgetResetDay(Context context) {
        return prefs(context).getInt(KEY_BUDGET_RESET_DAY, 1);
    }

    public static void saveBudgetResetDay(Context context, int day) {
        prefs(context).edit().putInt(KEY_BUDGET_RESET_DAY, day).apply();
    }
}
