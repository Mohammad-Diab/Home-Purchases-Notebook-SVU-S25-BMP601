package com.example.homepurchases.utils;

import android.content.Context;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {

    private static final String LABEL_NEW = "ل.س ج";
    private static final String LABEL_OLD = "ل.س ق";
    // 1 New SP = 100 Old SP
    private static final double NEW_TO_OLD = 100.0;

    public static String format(double amountInNewSP, Context context) {
        int currencyType = SettingsManager.getCurrencyType(context);
        double displayAmount = amountInNewSP;
        String label = LABEL_NEW;

        if (currencyType == SettingsManager.CURRENCY_OLD) {
            displayAmount = amountInNewSP * NEW_TO_OLD;
            label = LABEL_OLD;
        }

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("ar"));
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(0);

        return toArabicDigits(nf.format(displayAmount)) + " " + label;
    }

    public static String toArabicDigits(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append((char) ('\u0660' + (c - '0')));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // Formatted number without label — use for locked/read-only display fields
    public static String formatAmount(double amountInNewSP, Context context) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("ar"));
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(0);
        return toArabicDigits(nf.format(toDisplayAmount(amountInNewSP, context)));
    }

    // Returns the raw display amount without label (for editable input fields)
    public static double toDisplayAmount(double amountInNewSP, Context context) {
        if (SettingsManager.getCurrencyType(context) == SettingsManager.CURRENCY_OLD) {
            return amountInNewSP * NEW_TO_OLD;
        }
        return amountInNewSP;
    }

    // Converts user input back to New SP for storage
    public static double toStorageAmount(double displayAmount, Context context) {
        if (SettingsManager.getCurrencyType(context) == SettingsManager.CURRENCY_OLD) {
            return displayAmount / NEW_TO_OLD;
        }
        return displayAmount;
    }
}
