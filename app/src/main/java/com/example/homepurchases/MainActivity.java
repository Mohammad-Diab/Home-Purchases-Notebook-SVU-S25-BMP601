package com.example.homepurchases;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.homepurchases.utils.SoundManager;
import com.example.homepurchases.utils.ThemeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private SoundManager soundManager;

    @Override
    protected void attachBaseContext(Context base) {
        Locale arabic = new Locale("ar");
        Locale.setDefault(arabic);
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.setLocale(arabic);
        super.attachBaseContext(base.createConfigurationContext(config));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        AppBarConfiguration appBarConfig = new AppBarConfiguration.Builder(
                R.id.homeFragment, R.id.purchasesFragment, R.id.settingsFragment).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            boolean isTopLevel = id == R.id.homeFragment
                    || id == R.id.purchasesFragment
                    || id == R.id.settingsFragment;
            bottomNav.setVisibility(isTopLevel ? View.VISIBLE : View.GONE);
        });

        soundManager = new SoundManager(this);

        final int[] prevTopLevelId = {R.id.homeFragment};
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            boolean isTopLevel = id == R.id.homeFragment
                    || id == R.id.purchasesFragment
                    || id == R.id.settingsFragment;
            if (isTopLevel && id != prevTopLevelId[0]) {
                soundManager.playTab();
            }
            if (isTopLevel) prevTopLevelId[0] = id;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            if (id == R.id.addEditFragment
                    || id == R.id.statisticsFragment
                    || id == R.id.categoryManagementFragment) {
                soundManager.playOpen();
            }
            if (id == R.id.addEditFragment) {
                int purchaseId = arguments != null ? arguments.getInt("purchaseId", -1) : -1;
                getSupportActionBar().setTitle(purchaseId == -1
                        ? getString(R.string.add_purchase)
                        : getString(R.string.edit_purchase));
            }
        });
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    @Override
    public boolean onSupportNavigateUp() {
        int currentId = navController.getCurrentDestination() != null
                ? navController.getCurrentDestination().getId() : -1;
        if (currentId == R.id.addEditFragment
                || currentId == R.id.categoryManagementFragment
                || currentId == R.id.statisticsFragment) {
            soundManager.playCancel();
        }
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundManager.release();
    }
}
