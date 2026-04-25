package com.example.homepurchases.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;

import com.example.homepurchases.R;

public class ThemeManager {

    private static final int[][] THEME_MAP = {
            {R.style.Theme_HomePurchases_Light_0,
             R.style.Theme_HomePurchases_Light_1,
             R.style.Theme_HomePurchases_Light_2,
             R.style.Theme_HomePurchases_Light_3},
            {R.style.Theme_HomePurchases_Dark_0,
             R.style.Theme_HomePurchases_Dark_1,
             R.style.Theme_HomePurchases_Dark_2,
             R.style.Theme_HomePurchases_Dark_3}
    };

    public static void applyTheme(Activity activity) {
        int mode   = SettingsManager.getThemeMode(activity);
        int accent = SettingsManager.getThemeAccent(activity);
        if (mode == SettingsManager.MODE_AUTO) {
            int nightBits = activity.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            mode = (nightBits == Configuration.UI_MODE_NIGHT_YES)
                    ? SettingsManager.MODE_DARK : SettingsManager.MODE_LIGHT;
        }
        activity.setTheme(THEME_MAP[mode][accent]);
    }

    public static boolean resolvedDarkMode(Activity activity) {
        int mode = SettingsManager.getThemeMode(activity);
        if (mode == SettingsManager.MODE_AUTO) {
            int nightBits = activity.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            return nightBits == Configuration.UI_MODE_NIGHT_YES;
        }
        return mode == SettingsManager.MODE_DARK;
    }

    public static void restartApp(Activity activity) {
        Intent intent = activity.getIntent();
        activity.finish();
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
