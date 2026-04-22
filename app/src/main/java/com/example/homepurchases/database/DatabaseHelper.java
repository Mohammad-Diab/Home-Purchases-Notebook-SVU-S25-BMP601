package com.example.homepurchases.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "home_purchases.db";
    private static final int DB_VERSION = 2;

    public static final String TABLE_PURCHASES = "purchases";
    public static final String TABLE_CATEGORIES = "categories";

    // purchases columns
    public static final String COL_P_ID = "id";
    public static final String COL_P_ITEM_NAME = "item_name";
    public static final String COL_P_CATEGORY_ID = "category_id";
    public static final String COL_P_PRICE = "price";
    public static final String COL_P_QUANTITY = "quantity";
    public static final String COL_P_TOTAL_COST = "total_cost";
    public static final String COL_P_DATE = "date";
    public static final String COL_P_NOTES = "notes";

    // categories columns
    public static final String COL_C_ID = "id";
    public static final String COL_C_NAME = "name";
    public static final String COL_C_ICON_NAME = "icon_name";
    public static final String COL_C_DESCRIPTION = "description";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " (" +
                COL_C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_C_NAME + " TEXT NOT NULL, " +
                COL_C_ICON_NAME + " TEXT, " +
                COL_C_DESCRIPTION + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_PURCHASES + " (" +
                COL_P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_P_ITEM_NAME + " TEXT NOT NULL, " +
                COL_P_CATEGORY_ID + " INTEGER, " +
                COL_P_PRICE + " REAL, " +
                COL_P_QUANTITY + " INTEGER, " +
                COL_P_TOTAL_COST + " REAL, " +
                COL_P_DATE + " INTEGER, " +
                COL_P_NOTES + " TEXT, " +
                "FOREIGN KEY(" + COL_P_CATEGORY_ID + ") REFERENCES " +
                TABLE_CATEGORIES + "(" + COL_C_ID + "))");

        seedDefaultCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    private void seedDefaultCategories(SQLiteDatabase db) {
        String[][] categories = {
                {"طعام",        "ic_restaurant",      "المواد الغذائية"},
                {"تنظيف",       "ic_cleaning_services","مواد التنظيف"},
                {"أدوات",       "ic_build",            "الأدوات والمعدات"},
                {"ملابس",       "ic_checkroom",        "الملابس والأحذية"},
                {"إلكترونيات",  "ic_devices",          "الأجهزة الإلكترونية"},
                {"صحة",         "ic_local_hospital",   "الأدوية والصحة"},
                {"ترفيه",       "ic_sports_esports",   "الترفيه والهوايات"},
                {"فواتير",      "ic_receipt",          "الفواتير والمدفوعات"},
                {"أخرى",        "ic_category",         "مشتريات متنوعة"}
        };

        for (String[] cat : categories) {
            ContentValues cv = new ContentValues();
            cv.put(COL_C_NAME, cat[0]);
            cv.put(COL_C_ICON_NAME, cat[1]);
            cv.put(COL_C_DESCRIPTION, cat[2]);
            db.insert(TABLE_CATEGORIES, null, cv);
        }
    }
}
