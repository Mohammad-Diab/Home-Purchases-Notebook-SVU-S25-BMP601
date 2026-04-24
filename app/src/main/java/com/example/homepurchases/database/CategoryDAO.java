package com.example.homepurchases.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.homepurchases.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private static final String TAG = "CategoryDAO";
    private final SQLiteDatabase db;

    public CategoryDAO(Context context) {
        db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    public long insertCategory(Category category) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COL_C_NAME, category.getName());
            cv.put(DatabaseHelper.COL_C_ICON_NAME, category.getIconName());
            cv.put(DatabaseHelper.COL_C_DESCRIPTION, category.getDescription());
            return db.insert(DatabaseHelper.TABLE_CATEGORIES, null, cv);
        } catch (Exception e) {
            Log.e(TAG, "insertCategory failed: " + e.getMessage());
            return -1;
        }
    }

    public int updateCategory(Category category) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COL_C_NAME, category.getName());
            cv.put(DatabaseHelper.COL_C_ICON_NAME, category.getIconName());
            cv.put(DatabaseHelper.COL_C_DESCRIPTION, category.getDescription());
            return db.update(DatabaseHelper.TABLE_CATEGORIES, cv,
                    DatabaseHelper.COL_C_ID + "=?",
                    new String[]{String.valueOf(category.getId())});
        } catch (Exception e) {
            Log.e(TAG, "updateCategory failed: " + e.getMessage());
            return 0;
        }
    }

    public int deleteCategory(int id) {
        try {
            return db.delete(DatabaseHelper.TABLE_CATEGORIES,
                    DatabaseHelper.COL_C_ID + "=?",
                    new String[]{String.valueOf(id)});
        } catch (Exception e) {
            Log.e(TAG, "deleteCategory failed: " + e.getMessage());
            return 0;
        }
    }

    public boolean hasPurchases(int categoryId) {
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PURCHASES +
                    " WHERE " + DatabaseHelper.COL_P_CATEGORY_ID + "=?",
                    new String[]{String.valueOf(categoryId)});
            boolean result = false;
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0) > 0;
            }
            cursor.close();
            return result;
        } catch (Exception e) {
            Log.e(TAG, "hasPurchases failed: " + e.getMessage());
            return true;
        }
    }

    public List<Category> getAllCategories() {
        try {
            List<Category> list = new ArrayList<>();
            Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                    null, null, null, null, null,
                    DatabaseHelper.COL_C_NAME + " ASC");
            while (cursor.moveToNext()) {
                list.add(cursorToCategory(cursor));
            }
            cursor.close();
            return list;
        } catch (Exception e) {
            Log.e(TAG, "getAllCategories failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Category getCategoryById(int id) {
        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                    null,
                    DatabaseHelper.COL_C_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null);
            Category category = null;
            if (cursor.moveToFirst()) {
                category = cursorToCategory(cursor);
            }
            cursor.close();
            return category;
        } catch (Exception e) {
            Log.e(TAG, "getCategoryById failed: " + e.getMessage());
            return null;
        }
    }

    private Category cursorToCategory(Cursor cursor) {
        return new Category(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_C_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_C_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_C_ICON_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_C_DESCRIPTION))
        );
    }
}
