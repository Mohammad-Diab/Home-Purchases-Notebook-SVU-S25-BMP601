package com.example.homepurchases.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.homepurchases.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private final SQLiteDatabase db;

    public CategoryDAO(Context context) {
        db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    public long insertCategory(Category category) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_C_NAME, category.getName());
        cv.put(DatabaseHelper.COL_C_ICON_NAME, category.getIconName());
        cv.put(DatabaseHelper.COL_C_DESCRIPTION, category.getDescription());
        return db.insert(DatabaseHelper.TABLE_CATEGORIES, null, cv);
    }

    public int updateCategory(Category category) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_C_NAME, category.getName());
        cv.put(DatabaseHelper.COL_C_ICON_NAME, category.getIconName());
        cv.put(DatabaseHelper.COL_C_DESCRIPTION, category.getDescription());
        return db.update(DatabaseHelper.TABLE_CATEGORIES, cv,
                DatabaseHelper.COL_C_ID + "=?",
                new String[]{String.valueOf(category.getId())});
    }

    public int deleteCategory(int id) {
        return db.delete(DatabaseHelper.TABLE_CATEGORIES,
                DatabaseHelper.COL_C_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public boolean hasPurchases(int categoryId) {
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
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                null, null, null, null, null,
                DatabaseHelper.COL_C_NAME + " ASC");
        while (cursor.moveToNext()) {
            list.add(cursorToCategory(cursor));
        }
        cursor.close();
        return list;
    }

    public Category getCategoryById(int id) {
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
