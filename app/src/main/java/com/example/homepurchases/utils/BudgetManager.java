package com.example.homepurchases.utils;

import android.content.Context;

import com.example.homepurchases.database.PurchaseDAO;
import com.example.homepurchases.models.BudgetPeriod;

import java.util.Calendar;

public class BudgetManager {

    public static long getCurrentPeriodStart(Context context) {
        BudgetPeriod period = BudgetPeriod.fromValue(SettingsManager.getBudgetPeriod(context));
        int resetDay = SettingsManager.getBudgetResetDay(context);
        Calendar now = Calendar.getInstance();
        Calendar start = Calendar.getInstance();

        switch (period) {
            case DAILY:
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                start.set(Calendar.MILLISECOND, 0);
                break;

            case WEEKLY:
                int targetDayOfWeek = resetDayToCalendar(resetDay);
                start = (Calendar) now.clone();
                startOfDay(start);
                while (start.get(Calendar.DAY_OF_WEEK) != targetDayOfWeek) {
                    start.add(Calendar.DAY_OF_YEAR, -1);
                }
                break;

            case MONTHLY:
                int day = resetDay;
                start.set(Calendar.DAY_OF_MONTH, 1);
                startOfDay(start);
                int lastDayThisMonth = start.getActualMaximum(Calendar.DAY_OF_MONTH);
                int actualDay = Math.min(day, lastDayThisMonth);
                start.set(Calendar.DAY_OF_MONTH, actualDay);

                if (start.after(now)) {
                    start.add(Calendar.MONTH, -1);
                    int lastDayPrevMonth = start.getActualMaximum(Calendar.DAY_OF_MONTH);
                    start.set(Calendar.DAY_OF_MONTH, Math.min(day, lastDayPrevMonth));
                    startOfDay(start);
                }
                break;
        }

        return start.getTimeInMillis();
    }

    public static long getCurrentPeriodEnd(Context context) {
        BudgetPeriod period = BudgetPeriod.fromValue(SettingsManager.getBudgetPeriod(context));
        int resetDay = SettingsManager.getBudgetResetDay(context);
        long periodStart = getCurrentPeriodStart(context);
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(periodStart);

        switch (period) {
            case DAILY:
                end.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case WEEKLY:
                end.add(Calendar.DAY_OF_YEAR, 7);
                break;
            case MONTHLY:
                end.add(Calendar.MONTH, 1);
                int lastDay = end.getActualMaximum(Calendar.DAY_OF_MONTH);
                end.set(Calendar.DAY_OF_MONTH, Math.min(resetDay, lastDay));
                break;
        }

        end.add(Calendar.MILLISECOND, -1);
        return end.getTimeInMillis();
    }

    public static double getSpentThisPeriod(Context context) {
        long start = getCurrentPeriodStart(context);
        long end = getCurrentPeriodEnd(context);
        return new PurchaseDAO(context).getTotalBetween(start, end);
    }

    public static float getBudgetAmount(Context context) {
        return SettingsManager.getBudgetAmount(context);
    }

    public static boolean isBudgetSet(Context context) {
        return SettingsManager.getBudgetAmount(context) > 0;
    }

    public static long getPreviousPeriodStart(Context context) {
        BudgetPeriod period = BudgetPeriod.fromValue(SettingsManager.getBudgetPeriod(context));
        int resetDay = SettingsManager.getBudgetResetDay(context);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getCurrentPeriodStart(context));

        switch (period) {
            case DAILY:
                cal.add(Calendar.DAY_OF_YEAR, -1);
                break;
            case WEEKLY:
                int targetDay = resetDayToCalendar(resetDay);
                cal.add(Calendar.DAY_OF_YEAR, -1);
                while (cal.get(Calendar.DAY_OF_WEEK) != targetDay) {
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                }
                startOfDay(cal);
                break;
            case MONTHLY:
                cal.add(Calendar.MONTH, -1);
                int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                cal.set(Calendar.DAY_OF_MONTH, Math.min(resetDay, lastDay));
                startOfDay(cal);
                break;
        }
        return cal.getTimeInMillis();
    }

    public static double getSpentLastPeriod(Context context) {
        long start = getPreviousPeriodStart(context);
        long end   = getCurrentPeriodStart(context) - 1;
        return new PurchaseDAO(context).getTotalBetween(start, end);
    }

    public static boolean isOverBudget(Context context) {
        if (!isBudgetSet(context)) return false;
        return getSpentThisPeriod(context) > getBudgetAmount(context);
    }

    private static int resetDayToCalendar(int resetDay) {
        switch (resetDay) {
            case 0: return Calendar.SATURDAY;
            case 1: return Calendar.SUNDAY;
            case 2: return Calendar.MONDAY;
            case 3: return Calendar.TUESDAY;
            case 4: return Calendar.WEDNESDAY;
            case 5: return Calendar.THURSDAY;
            case 6: return Calendar.FRIDAY;
            default: return Calendar.SATURDAY;
        }
    }

    private static void startOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }
}
