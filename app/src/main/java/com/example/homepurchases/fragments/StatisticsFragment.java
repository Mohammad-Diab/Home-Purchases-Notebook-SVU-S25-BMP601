package com.example.homepurchases.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.homepurchases.MainActivity;
import com.example.homepurchases.R;
import com.example.homepurchases.database.CategoryDAO;
import com.example.homepurchases.database.PurchaseDAO;
import com.example.homepurchases.models.Category;
import com.example.homepurchases.utils.CurrencyFormatter;
import com.example.homepurchases.utils.SettingsManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    // 0 = by category, 1 = by date
    private static final int VIEW_CATEGORY = 0;
    private static final int VIEW_DATE     = 1;

    // period type indices matching stats_period_options
    private static final int PERIOD_DAY   = 0;
    private static final int PERIOD_WEEK  = 1;
    private static final int PERIOD_MONTH = 2;
    private static final int PERIOD_YEAR  = 3;

    private TextView tvTotalAmount, tvNoData;
    private LinearLayout layoutBreakdown;
    private Spinner spinnerView, spinnerPeriod;

    private PurchaseDAO purchaseDAO;
    private CategoryDAO categoryDAO;

    private boolean isInitializing = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        purchaseDAO    = new PurchaseDAO(requireContext());
        categoryDAO    = new CategoryDAO(requireContext());
        tvTotalAmount  = view.findViewById(R.id.tv_total_amount);
        tvNoData       = view.findViewById(R.id.tv_no_data);
        layoutBreakdown = view.findViewById(R.id.layout_category_breakdown);
        spinnerView    = view.findViewById(R.id.spinner_stats_view);
        spinnerPeriod  = view.findViewById(R.id.spinner_stats_period);

        requireActivity().setTitle(R.string.statistics_title);

        setupSpinners();

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).getSoundManager().playCancel();
                        }
                        setEnabled(false);
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                });
    }

    private void setupSpinners() {
        // View switcher
        ArrayAdapter<String> viewAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.stats_view_options)));
        viewAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerView.setAdapter(viewAdapter);

        // Period spinner
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.stats_period_options)));
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(periodAdapter);

        // Default period = budget period from settings (Daily→0, Weekly→1, Monthly→2)
        int budgetPeriod = SettingsManager.getBudgetPeriod(requireContext());
        int defaultPeriod = (budgetPeriod >= PERIOD_DAY && budgetPeriod <= PERIOD_MONTH)
                ? budgetPeriod : PERIOD_MONTH;
        spinnerPeriod.setSelection(defaultPeriod);

        isInitializing = true;
        spinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                if (pos == VIEW_DATE) {
                    spinnerPeriod.setVisibility(View.VISIBLE);
                } else {
                    spinnerPeriod.setVisibility(View.GONE);
                }
                if (!isInitializing) loadStatistics();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                if (!isInitializing) loadStatistics();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        isInitializing = false;
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            double total = purchaseDAO.getTotalExpenses();
            tvTotalAmount.setText(CurrencyFormatter.format(total, requireContext()));

            int selectedView = spinnerView.getSelectedItemPosition();
            if (selectedView == VIEW_DATE) {
                loadDateBreakdown(spinnerPeriod.getSelectedItemPosition());
            } else {
                loadCategoryBreakdown(total);
            }
        } catch (Exception e) {
            Log.e("StatisticsFragment", "loadStatistics failed: " + e.getMessage());
            Toast.makeText(requireContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCategoryBreakdown(double total) {
        Map<Integer, Double> expenseMap = purchaseDAO.getExpensesByCategory();
        layoutBreakdown.removeAllViews();

        if (expenseMap.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            return;
        }
        tvNoData.setVisibility(View.GONE);

        List<Category> allCategories = categoryDAO.getAllCategories();
        for (Map.Entry<Integer, Double> entry : expenseMap.entrySet()) {
            String catName = findCategoryName(allCategories, entry.getKey());
            double amount  = entry.getValue();
            int percent    = total > 0 ? (int) Math.round((amount / total) * 100) : 0;
            addBreakdownRow(catName, amount, percent);
        }
    }

    private void loadDateBreakdown(int periodType) {
        Map<String, Double> periodMap = purchaseDAO.getExpensesByPeriod(periodType);
        layoutBreakdown.removeAllViews();

        if (periodMap.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            return;
        }
        tvNoData.setVisibility(View.GONE);

        double totalAmount = 0;
        for (double v : periodMap.values()) totalAmount += v;

        for (Map.Entry<String, Double> entry : periodMap.entrySet()) {
            String label = formatPeriodLabel(entry.getKey(), periodType);
            double amount = entry.getValue();
            int percent   = totalAmount > 0 ? (int) Math.round((amount / totalAmount) * 100) : 0;
            addBreakdownRow(label, amount, percent);
        }
    }

    private void addBreakdownRow(String label, double amount, int percent) {
        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_stats_category, layoutBreakdown, false);
        ((TextView)   row.findViewById(R.id.tv_cat_name)).setText(label);
        ((TextView)   row.findViewById(R.id.tv_cat_amount))
                .setText(CurrencyFormatter.format(amount, requireContext()));
        ((ProgressBar) row.findViewById(R.id.pb_category)).setProgress(percent);
        layoutBreakdown.addView(row);
    }

    private String formatPeriodLabel(String sqlPeriod, int periodType) {
        switch (periodType) {
            case PERIOD_DAY:
                // "2025-01-15" → "٢٠٢٥/٠١/١٥"
                return CurrencyFormatter.toArabicDigits(sqlPeriod.replace("-", "/"));
            case PERIOD_WEEK: {
                // "2025-03" → "أسبوع ٠٣ / ٢٠٢٥"
                String[] parts = sqlPeriod.split("-");
                if (parts.length == 2) {
                    return getString(R.string.stats_week_label,
                            CurrencyFormatter.toArabicDigits(parts[1]),
                            CurrencyFormatter.toArabicDigits(parts[0]));
                }
                return CurrencyFormatter.toArabicDigits(sqlPeriod);
            }
            case PERIOD_MONTH:
                // "2025-01" → "٢٠٢٥/٠١"
                return CurrencyFormatter.toArabicDigits(sqlPeriod.replace("-", "/"));
            case PERIOD_YEAR:
                // "2025" → "٢٠٢٥"
                return CurrencyFormatter.toArabicDigits(sqlPeriod);
            default:
                return CurrencyFormatter.toArabicDigits(sqlPeriod);
        }
    }

    private String findCategoryName(List<Category> categories, int id) {
        for (Category c : categories) {
            if (c.getId() == id) return c.getName();
        }
        return String.valueOf(id);
    }
}
