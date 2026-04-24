package com.example.homepurchases.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.homepurchases.models.Purchase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PurchaseDAO {

    private static final String TAG = "PurchaseDAO";
    private final SQLiteDatabase db;

    public PurchaseDAO(Context context) {
        db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    public long insertPurchase(Purchase purchase) {
        try {
            ContentValues cv = buildContentValues(purchase);
            return db.insert(DatabaseHelper.TABLE_PURCHASES, null, cv);
        } catch (Exception e) {
            Log.e(TAG, "insertPurchase failed: " + e.getMessage());
            return -1;
        }
    }

    public int updatePurchase(Purchase purchase) {
        try {
            ContentValues cv = buildContentValues(purchase);
            return db.update(DatabaseHelper.TABLE_PURCHASES, cv,
                    DatabaseHelper.COL_P_ID + "=?",
                    new String[]{String.valueOf(purchase.getId())});
        } catch (Exception e) {
            Log.e(TAG, "updatePurchase failed: " + e.getMessage());
            return 0;
        }
    }

    public int deletePurchase(int id) {
        try {
            return db.delete(DatabaseHelper.TABLE_PURCHASES,
                    DatabaseHelper.COL_P_ID + "=?",
                    new String[]{String.valueOf(id)});
        } catch (Exception e) {
            Log.e(TAG, "deletePurchase failed: " + e.getMessage());
            return 0;
        }
    }

    public List<Purchase> getAllPurchases() {
        try {
            return queryPurchases(null, null);
        } catch (Exception e) {
            Log.e(TAG, "getAllPurchases failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Purchase getPurchaseById(int id) {
        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_PURCHASES,
                    null,
                    DatabaseHelper.COL_P_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null);
            Purchase purchase = null;
            if (cursor.moveToFirst()) {
                purchase = cursorToPurchase(cursor);
            }
            cursor.close();
            return purchase;
        } catch (Exception e) {
            Log.e(TAG, "getPurchaseById failed: " + e.getMessage());
            return null;
        }
    }

    public List<Purchase> getPurchasesByCategory(int categoryId) {
        try {
            return queryPurchases(
                    DatabaseHelper.COL_P_CATEGORY_ID + "=?",
                    new String[]{String.valueOf(categoryId)});
        } catch (Exception e) {
            Log.e(TAG, "getPurchasesByCategory failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Purchase> getPurchasesByDateRange(long startDate, long endDate) {
        try {
            return queryPurchases(
                    DatabaseHelper.COL_P_DATE + " BETWEEN ? AND ?",
                    new String[]{String.valueOf(startDate), String.valueOf(endDate)});
        } catch (Exception e) {
            Log.e(TAG, "getPurchasesByDateRange failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Purchase> getFilteredPurchases(
            Integer categoryId, Long startDate, Long endDate, String query) {
        try {
            StringBuilder where = new StringBuilder();
            List<String> args = new ArrayList<>();

            if (categoryId != null) {
                where.append(DatabaseHelper.COL_P_CATEGORY_ID).append("=?");
                args.add(String.valueOf(categoryId));
            }
            if (startDate != null) {
                if (where.length() > 0) where.append(" AND ");
                where.append(DatabaseHelper.COL_P_DATE).append(" BETWEEN ? AND ?");
                args.add(String.valueOf(startDate));
                args.add(String.valueOf(endDate));
            }
            if (query != null && !query.isEmpty()) {
                if (where.length() > 0) where.append(" AND ");
                where.append(DatabaseHelper.COL_P_ITEM_NAME).append(" LIKE ?");
                args.add("%" + query + "%");
            }

            String selection = where.length() > 0 ? where.toString() : null;
            String[] selectionArgs = args.isEmpty() ? null : args.toArray(new String[0]);
            return queryPurchases(selection, selectionArgs);
        } catch (Exception e) {
            Log.e(TAG, "getFilteredPurchases failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public long getEarliestPurchaseDate() {
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT MIN(" + DatabaseHelper.COL_P_DATE + ") FROM " +
                    DatabaseHelper.TABLE_PURCHASES, null);
            long result = -1;
            if (cursor.moveToFirst() && !cursor.isNull(0)) result = cursor.getLong(0);
            cursor.close();
            return result;
        } catch (Exception e) {
            Log.e(TAG, "getEarliestPurchaseDate failed: " + e.getMessage());
            return -1;
        }
    }

    public long getLatestPurchaseDate() {
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT MAX(" + DatabaseHelper.COL_P_DATE + ") FROM " +
                    DatabaseHelper.TABLE_PURCHASES, null);
            long result = -1;
            if (cursor.moveToFirst() && !cursor.isNull(0)) result = cursor.getLong(0);
            cursor.close();
            return result;
        } catch (Exception e) {
            Log.e(TAG, "getLatestPurchaseDate failed: " + e.getMessage());
            return -1;
        }
    }

    public Set<Integer> getCategoryIdsWithPurchases() {
        Set<Integer> ids = new HashSet<>();
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT DISTINCT " + DatabaseHelper.COL_P_CATEGORY_ID +
                    " FROM " + DatabaseHelper.TABLE_PURCHASES, null);
            while (cursor.moveToNext()) ids.add(cursor.getInt(0));
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "getCategoryIdsWithPurchases failed: " + e.getMessage());
        }
        return ids;
    }

    public List<Purchase> searchPurchases(String query) {
        try {
            return queryPurchases(
                    DatabaseHelper.COL_P_ITEM_NAME + " LIKE ?",
                    new String[]{"%" + query + "%"});
        } catch (Exception e) {
            Log.e(TAG, "searchPurchases failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public double getTotalExpenses() {
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT SUM(" + DatabaseHelper.COL_P_TOTAL_COST + ") FROM " +
                    DatabaseHelper.TABLE_PURCHASES, null);
            double total = 0;
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                total = cursor.getDouble(0);
            }
            cursor.close();
            return total;
        } catch (Exception e) {
            Log.e(TAG, "getTotalExpenses failed: " + e.getMessage());
            return 0.0;
        }
    }

    public double getTotalBetween(long startDate, long endDate) {
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT SUM(" + DatabaseHelper.COL_P_TOTAL_COST + ") FROM " +
                    DatabaseHelper.TABLE_PURCHASES +
                    " WHERE " + DatabaseHelper.COL_P_DATE + " BETWEEN ? AND ?",
                    new String[]{String.valueOf(startDate), String.valueOf(endDate)});
            double total = 0;
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                total = cursor.getDouble(0);
            }
            cursor.close();
            return total;
        } catch (Exception e) {
            Log.e(TAG, "getTotalBetween failed: " + e.getMessage());
            return 0.0;
        }
    }

    public Map<String, Double> getExpensesByPeriod(int periodType) {
        try {
            String fmt;
            switch (periodType) {
                case 0:  fmt = "%Y-%m-%d"; break;
                case 1:  fmt = "%Y-%W";    break;
                case 2:  fmt = "%Y-%m";    break;
                case 3:  fmt = "%Y";       break;
                default: fmt = "%Y-%m-%d"; break;
            }
            Map<String, Double> map = new LinkedHashMap<>();
            Cursor cursor = db.rawQuery(
                    "SELECT strftime('" + fmt + "', " +
                    DatabaseHelper.COL_P_DATE + "/1000, 'unixepoch') AS period" +
                    ", SUM(" + DatabaseHelper.COL_P_TOTAL_COST + ") AS total" +
                    " FROM " + DatabaseHelper.TABLE_PURCHASES +
                    " GROUP BY period" +
                    " ORDER BY period DESC",
                    null);
            while (cursor.moveToNext()) {
                map.put(cursor.getString(0), cursor.getDouble(1));
            }
            cursor.close();
            return map;
        } catch (Exception e) {
            Log.e(TAG, "getExpensesByPeriod failed: " + e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    public Map<Integer, Double> getExpensesByCategory() {
        try {
            Map<Integer, Double> map = new LinkedHashMap<>();
            Cursor cursor = db.rawQuery(
                    "SELECT " + DatabaseHelper.COL_P_CATEGORY_ID +
                    ", SUM(" + DatabaseHelper.COL_P_TOTAL_COST + ") FROM " +
                    DatabaseHelper.TABLE_PURCHASES +
                    " GROUP BY " + DatabaseHelper.COL_P_CATEGORY_ID +
                    " ORDER BY SUM(" + DatabaseHelper.COL_P_TOTAL_COST + ") DESC",
                    null);
            while (cursor.moveToNext()) {
                map.put(cursor.getInt(0), cursor.getDouble(1));
            }
            cursor.close();
            return map;
        } catch (Exception e) {
            Log.e(TAG, "getExpensesByCategory failed: " + e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    public Map<Integer, Double> getExpensesByCategoryBetween(long startDate, long endDate) {
        try {
            Map<Integer, Double> map = new LinkedHashMap<>();
            Cursor cursor = db.rawQuery(
                    "SELECT " + DatabaseHelper.COL_P_CATEGORY_ID +
                    ", SUM(" + DatabaseHelper.COL_P_TOTAL_COST + ") FROM " +
                    DatabaseHelper.TABLE_PURCHASES +
                    " WHERE " + DatabaseHelper.COL_P_DATE + " BETWEEN ? AND ?" +
                    " GROUP BY " + DatabaseHelper.COL_P_CATEGORY_ID +
                    " ORDER BY SUM(" + DatabaseHelper.COL_P_TOTAL_COST + ") DESC",
                    new String[]{String.valueOf(startDate), String.valueOf(endDate)});
            while (cursor.moveToNext()) {
                map.put(cursor.getInt(0), cursor.getDouble(1));
            }
            cursor.close();
            return map;
        } catch (Exception e) {
            Log.e(TAG, "getExpensesByCategoryBetween failed: " + e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    public List<Purchase> getRecentPurchases(int limit) {
        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_PURCHASES,
                    null, null, null, null, null,
                    DatabaseHelper.COL_P_DATE + " DESC",
                    String.valueOf(limit));
            List<Purchase> list = new ArrayList<>();
            while (cursor.moveToNext()) {
                list.add(cursorToPurchase(cursor));
            }
            cursor.close();
            return list;
        } catch (Exception e) {
            Log.e(TAG, "getRecentPurchases failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public int deleteAllPurchases() {
        try {
            return db.delete(DatabaseHelper.TABLE_PURCHASES, null, null);
        } catch (Exception e) {
            Log.e(TAG, "deleteAllPurchases failed: " + e.getMessage());
            return 0;
        }
    }

    public int getPurchaseCount() {
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PURCHASES, null);
            int count = 0;
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
            return count;
        } catch (Exception e) {
            Log.e(TAG, "getPurchaseCount failed: " + e.getMessage());
            return 0;
        }
    }

    private List<Purchase> queryPurchases(String selection, String[] selectionArgs) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_PURCHASES,
                null, selection, selectionArgs, null, null,
                DatabaseHelper.COL_P_DATE + " DESC");
        List<Purchase> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(cursorToPurchase(cursor));
        }
        cursor.close();
        return list;
    }

    private ContentValues buildContentValues(Purchase purchase) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_P_ITEM_NAME, purchase.getItemName());
        cv.put(DatabaseHelper.COL_P_CATEGORY_ID, purchase.getCategoryId());
        cv.put(DatabaseHelper.COL_P_PRICE, purchase.getPrice());
        cv.put(DatabaseHelper.COL_P_QUANTITY, purchase.getQuantity());
        cv.put(DatabaseHelper.COL_P_TOTAL_COST, purchase.getPrice() * purchase.getQuantity());
        cv.put(DatabaseHelper.COL_P_DATE, purchase.getDate());
        cv.put(DatabaseHelper.COL_P_NOTES, purchase.getNotes());
        return cv;
    }

    private Purchase cursorToPurchase(Cursor cursor) {
        return new Purchase(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_P_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_P_ITEM_NAME)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_P_CATEGORY_ID)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_P_PRICE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_P_QUANTITY)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_P_TOTAL_COST)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_P_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_P_NOTES))
        );
    }
}
