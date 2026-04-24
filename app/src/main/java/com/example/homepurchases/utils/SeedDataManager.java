package com.example.homepurchases.utils;

import android.content.Context;
import android.util.Log;

import com.example.homepurchases.database.CategoryDAO;
import com.example.homepurchases.database.PurchaseDAO;
import com.example.homepurchases.models.Category;
import com.example.homepurchases.models.Purchase;

import java.util.List;

public class SeedDataManager {

    private static final String TAG = "SeedDataManager";
    private static final int GUARD_THRESHOLD = 20;
    private static final long DAY_MS = 86_400_000L;

    private static final String[] ITEM_NAMES = {
            "خبز وخضار طازجة",
            "مسحوق غسيل ومنظفات",
            "مروحة كهربائية",
            "بنطال جينز",
            "حاسوب محمول",
            "دواء للضغط",
            "مسكّنات ألم",
            "أدوات نجارة",
            "تلفاز ذكي",
            "قمصان صيفية",
            "بقالة أسبوعية",
            "فاتورة كهرباء",
            "اشتراك إنترنت",
            "اشتراك بث ترفيهي",
            "منظف أرضيات"
    };

    private static final int[] CAT_INDICES = {
            6, 4, 2, 8, 2, 5, 0, 1, 2, 8, 6, 7, 7, 3, 4
    };

    private static final double[] PRICES = {
            600,
            1500,
            4000,
            3000,
            65000,
            800,
            250,
            5000,
            25000,
            1200,
            1800,
            1500,
            3000,
            500,
            800
    };

    private static final int[] QUANTITIES = {
            2, 1, 1, 1, 1, 3, 5, 1, 1, 2, 4, 1, 1, 2, 1
    };

    private static final int[] DAYS_AGO = {
            1, 3, 5, 8, 12, 15, 18, 22, 27, 31, 36, 40, 44, 50, 57
    };

    private static final String[] NOTES = {
            null, null, "للغرفة الرئيسية", null, "للعمل من المنزل",
            null, null, null, null, null,
            "احتياجات الأسبوع", null, null, "اشتراك شهري", null
    };

    public static boolean seedTestData(Context context) {
        try {
            PurchaseDAO purchaseDAO = new PurchaseDAO(context);
            if (purchaseDAO.getPurchaseCount() >= ITEM_NAMES.length) {
                return false;
            }

            CategoryDAO categoryDAO = new CategoryDAO(context);
            List<Category> categories = categoryDAO.getAllCategories();
            if (categories.isEmpty()) return false;

            long now = System.currentTimeMillis();

            for (int i = 0; i < ITEM_NAMES.length; i++) {
                int catIdx = Math.min(CAT_INDICES[i], categories.size() - 1);
                Category cat = categories.get(catIdx);
                double price = PRICES[i];
                int qty = QUANTITIES[i];
                long date = now - (DAYS_AGO[i] * DAY_MS);

                Purchase p = new Purchase(
                        0,
                        ITEM_NAMES[i],
                        cat.getId(),
                        price,
                        qty,
                        price * qty,
                        date,
                        NOTES[i]
                );
                purchaseDAO.insertPurchase(p);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "seedTestData failed: " + e.getMessage());
            return false;
        }
    }
}
