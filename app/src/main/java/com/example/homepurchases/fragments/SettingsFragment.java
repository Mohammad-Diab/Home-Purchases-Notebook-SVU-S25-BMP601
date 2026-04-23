package com.example.homepurchases.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.homepurchases.R;
import com.example.homepurchases.utils.CurrencyFormatter;
import com.example.homepurchases.utils.SeedDataManager;
import com.example.homepurchases.utils.SettingsManager;
import com.example.homepurchases.utils.ThemeManager;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private Spinner spinnerDarkMode;
    private TextView tvAccentName, tvBudgetLocked;
    private AppCompatImageView[] accentSwatches;
    private TextInputEditText etBudgetAmount;
    private Spinner spinnerPeriod, spinnerResetDay;
    private LinearLayout layoutResetDay;
    private RadioGroup rgCurrency;

    private boolean isInitializing = true;

    private static final int[] LIGHT_COLORS = {
            R.color.accent_light_0, R.color.accent_light_1,
            R.color.accent_light_2, R.color.accent_light_3
    };
    private static final int[] DARK_COLORS = {
            R.color.accent_dark_0, R.color.accent_dark_1,
            R.color.accent_dark_2, R.color.accent_dark_3
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().setTitle(R.string.settings_title);
        bindViews(view);
        populateControls();
        setupListeners(view);
        isInitializing = false;
    }

    private void bindViews(View view) {
        spinnerDarkMode = view.findViewById(R.id.spinner_dark_mode);
        tvAccentName    = view.findViewById(R.id.tv_accent_name);
        etBudgetAmount  = view.findViewById(R.id.et_budget_amount);
        tvBudgetLocked  = view.findViewById(R.id.tv_budget_locked);
        spinnerPeriod   = view.findViewById(R.id.spinner_period);
        spinnerResetDay = view.findViewById(R.id.spinner_reset_day);
        layoutResetDay  = view.findViewById(R.id.layout_reset_day);
        rgCurrency      = view.findViewById(R.id.rg_currency);

        accentSwatches = new AppCompatImageView[]{
                view.findViewById(R.id.accent_swatch_0),
                view.findViewById(R.id.accent_swatch_1),
                view.findViewById(R.id.accent_swatch_2),
                view.findViewById(R.id.accent_swatch_3)
        };
    }

    private void populateControls() {
        int mode   = SettingsManager.getThemeMode(requireContext());
        int accent = SettingsManager.getThemeAccent(requireContext());
        boolean isDark = mode == SettingsManager.MODE_DARK;

        // Mode spinner
        List<String> modeItems = Arrays.asList(
                getString(R.string.settings_mode_light),
                getString(R.string.settings_mode_dark));
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, modeItems);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDarkMode.setAdapter(modeAdapter);
        spinnerDarkMode.setSelection(mode);

        // Accent swatches
        updateSwatches(isDark, accent);

        // Budget amount
        float budgetNewSP = SettingsManager.getBudgetAmount(requireContext());
        if (budgetNewSP > 0) {
            double display = CurrencyFormatter.toDisplayAmount(budgetNewSP, requireContext());
            etBudgetAmount.setText(String.valueOf(display));
            etBudgetAmount.setEnabled(false);
            tvBudgetLocked.setVisibility(View.VISIBLE);
        }

        // Period spinner
        List<String> periods = Arrays.asList(
                getString(R.string.period_daily),
                getString(R.string.period_weekly),
                getString(R.string.period_monthly));
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, periods);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(periodAdapter);

        int savedPeriod = SettingsManager.getBudgetPeriod(requireContext());
        spinnerPeriod.setSelection(savedPeriod);

        // Reset day spinner
        updateResetDaySpinner(savedPeriod,
                SettingsManager.getBudgetResetDay(requireContext()));

        // Currency
        int currency = SettingsManager.getCurrencyType(requireContext());
        rgCurrency.check(currency == SettingsManager.CURRENCY_NEW
                ? R.id.rb_currency_new : R.id.rb_currency_old);
    }

    private void updateSwatches(boolean isDark, int selectedAccent) {
        int[] colorRes = isDark ? DARK_COLORS : LIGHT_COLORS;
        String[] names = getResources().getStringArray(
                isDark ? R.array.accent_names_dark : R.array.accent_names_light);

        for (int i = 0; i < 4; i++) {
            int color = ContextCompat.getColor(requireContext(), colorRes[i]);
            accentSwatches[i].setBackgroundTintList(ColorStateList.valueOf(color));
            accentSwatches[i].setScaleX(1.0f);
            accentSwatches[i].setScaleY(1.0f);
            if (i == selectedAccent) {
                accentSwatches[i].setForeground(
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_accent_swatch_selected));
                accentSwatches[i].setImageResource(R.drawable.ic_done);
                accentSwatches[i].setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), android.R.color.white)));
                accentSwatches[i].setPadding(8, 8, 8, 8);
            } else {
                accentSwatches[i].setForeground(null);
                accentSwatches[i].setImageDrawable(null);
                accentSwatches[i].setPadding(0, 0, 0, 0);
            }
        }
        tvAccentName.setText(names[selectedAccent]);
    }

    private void updateResetDaySpinner(int period, int savedResetDay) {
        if (period == 0) { // DAILY
            layoutResetDay.setVisibility(View.GONE);
            return;
        }
        layoutResetDay.setVisibility(View.VISIBLE);

        List<String> items;
        if (period == 1) { // WEEKLY
            items = Arrays.asList(getResources().getStringArray(R.array.week_days));
        } else { // MONTHLY
            NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("ar"));
            items = new ArrayList<>();
            for (int i = 1; i <= 31; i++) items.add(nf.format(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerResetDay.setAdapter(adapter);

        int position = period == 1
                ? savedResetDay
                : Math.max(0, savedResetDay - 1);
        if (position < items.size()) spinnerResetDay.setSelection(position);
    }

    private void setupListeners(View view) {
        // Display mode spinner
        spinnerDarkMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                if (isInitializing) return;
                SettingsManager.saveThemeMode(requireContext(), pos);
                ThemeManager.restartApp(requireActivity());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Accent swatches
        for (int i = 0; i < 4; i++) {
            final int index = i;
            accentSwatches[i].setOnClickListener(v -> {
                if (isInitializing) return;
                SettingsManager.saveThemeAccent(requireContext(), index);
                ThemeManager.restartApp(requireActivity());
            });
        }

        // Budget amount — save on focus lost
        etBudgetAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !isInitializing) saveBudgetAmount();
        });

        // Period spinner
        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                if (isInitializing) return;
                SettingsManager.saveBudgetPeriod(requireContext(), pos);
                updateResetDaySpinner(pos, 0);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Reset day spinner
        spinnerResetDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                if (isInitializing) return;
                int period = spinnerPeriod.getSelectedItemPosition();
                int day = (period == 1) ? pos : pos + 1;
                SettingsManager.saveBudgetResetDay(requireContext(), day);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Currency
        rgCurrency.setOnCheckedChangeListener((group, checkedId) -> {
            if (isInitializing) return;
            int type = checkedId == R.id.rb_currency_new
                    ? SettingsManager.CURRENCY_NEW
                    : SettingsManager.CURRENCY_OLD;
            SettingsManager.saveCurrencyType(requireContext(), type);
        });

        // Manage categories
        view.findViewById(R.id.btn_manage_categories).setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_settings_to_categories));

        // Seed test data
        view.findViewById(R.id.btn_seed_test_data).setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle(R.string.seed_confirm_title)
                        .setMessage(R.string.seed_confirm_message)
                        .setPositiveButton(R.string.btn_confirm, (d, w) -> {
                            boolean seeded = SeedDataManager.seedTestData(requireContext());
                            Toast.makeText(requireContext(),
                                    seeded ? R.string.seed_success : R.string.seed_data_exists,
                                    Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show());

        // Reset all data
        view.findViewById(R.id.btn_reset_data).setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle(R.string.reset_confirm_title)
                        .setMessage(R.string.reset_confirm_message)
                        .setPositiveButton(R.string.btn_confirm, (d, w) -> {
                            new com.example.homepurchases.database.PurchaseDAO(requireContext())
                                    .deleteAllPurchases();
                            Toast.makeText(requireContext(),
                                    R.string.reset_success, Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show());
    }

    private void saveBudgetAmount() {
        // Once a budget is set it cannot be changed
        if (SettingsManager.getBudgetAmount(requireContext()) > 0) return;

        String text = etBudgetAmount.getText().toString().trim();
        if (text.isEmpty()) return;

        try {
            double displayAmount = Double.parseDouble(text);
            if (displayAmount <= 0) return;
            double newSP = CurrencyFormatter.toStorageAmount(displayAmount, requireContext());
            SettingsManager.saveBudgetAmount(requireContext(), (float) newSP);
            etBudgetAmount.setEnabled(false);
            tvBudgetLocked.setVisibility(View.VISIBLE);
        } catch (NumberFormatException e) {
            Log.e("SettingsFragment", "invalid budget input: " + e.getMessage());
        }
    }
}
