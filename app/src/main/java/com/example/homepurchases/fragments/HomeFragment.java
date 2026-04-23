package com.example.homepurchases.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.homepurchases.R;
import com.example.homepurchases.database.CategoryDAO;
import com.example.homepurchases.database.PurchaseDAO;
import com.example.homepurchases.models.Category;
import com.example.homepurchases.models.Purchase;
import com.example.homepurchases.utils.BudgetManager;
import com.example.homepurchases.utils.CurrencyFormatter;
import com.example.homepurchases.utils.SettingsManager;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private CircularProgressIndicator progressBudget;
    private TextView tvSpentAmount, tvPeriodLabel, tvNoBudget;
    private TextView tvTotalEver, tvLastPeriod;
    private LinearLayout layoutRecentPurchases;
    private TextView tvHomeEmpty;

    private PurchaseDAO purchaseDAO;
    private CategoryDAO categoryDAO;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy/MM/dd", new Locale("ar"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        purchaseDAO           = new PurchaseDAO(requireContext());
        categoryDAO           = new CategoryDAO(requireContext());
        progressBudget        = view.findViewById(R.id.progress_budget);
        tvSpentAmount         = view.findViewById(R.id.tv_spent_amount);
        tvPeriodLabel         = view.findViewById(R.id.tv_period_label);
        tvNoBudget            = view.findViewById(R.id.tv_no_budget);
        tvTotalEver           = view.findViewById(R.id.tv_total_ever);
        tvLastPeriod          = view.findViewById(R.id.tv_last_period);
        layoutRecentPurchases = view.findViewById(R.id.layout_recent_purchases);
        tvHomeEmpty           = view.findViewById(R.id.tv_home_empty);

        requireActivity().setTitle(R.string.tab_home);

        view.findViewById(R.id.btn_view_stats).setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_home_to_statistics));

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        try {
            loadBudgetSection();
            loadStatsSection();
            loadRecentPurchases();
        } catch (Exception e) {
            Log.e("HomeFragment", "loadData failed: " + e.getMessage());
            Toast.makeText(requireContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBudgetSection() {
        if (!BudgetManager.isBudgetSet(requireContext())) {
            progressBudget.setProgress(0);
            tvSpentAmount.setText(CurrencyFormatter.format(0, requireContext()));
            tvPeriodLabel.setVisibility(View.GONE);
            tvNoBudget.setVisibility(View.VISIBLE);
            return;
        }

        tvNoBudget.setVisibility(View.GONE);
        tvPeriodLabel.setVisibility(View.VISIBLE);

        double spent  = BudgetManager.getSpentThisPeriod(requireContext());
        double budget = BudgetManager.getBudgetAmount(requireContext());
        int percent   = budget > 0 ? (int) Math.min(100, Math.round((spent / budget) * 100)) : 0;

        progressBudget.setProgress(percent);
        tvSpentAmount.setText(CurrencyFormatter.format(spent, requireContext()));
        tvPeriodLabel.setText(
                CurrencyFormatter.format(spent, requireContext())
                + " / "
                + CurrencyFormatter.format(budget, requireContext()));

        boolean isDark = SettingsManager.getThemeMode(requireContext()) == SettingsManager.MODE_DARK;
        int indicatorColor;
        if (BudgetManager.isOverBudget(requireContext())) {
            indicatorColor = ContextCompat.getColor(requireContext(),
                    isDark ? R.color.error_dark : R.color.error_light);
        } else {
            int accent = SettingsManager.getThemeAccent(requireContext());
            int[] accentColors = isDark
                    ? new int[]{R.color.accent_dark_0, R.color.accent_dark_1, R.color.accent_dark_2, R.color.accent_dark_3}
                    : new int[]{R.color.accent_light_0, R.color.accent_light_1, R.color.accent_light_2, R.color.accent_light_3};
            indicatorColor = ContextCompat.getColor(requireContext(), accentColors[accent]);
        }
        progressBudget.setIndicatorColor(indicatorColor);
    }

    private void loadStatsSection() {
        double totalEver = purchaseDAO.getTotalExpenses();
        tvTotalEver.setText(CurrencyFormatter.format(totalEver, requireContext()));

        long currentStart = BudgetManager.getCurrentPeriodStart(requireContext());
        long prevEnd      = currentStart - 1;
        int periodVal     = SettingsManager.getBudgetPeriod(requireContext());
        long prevStart;
        if (periodVal == 0) {
            prevStart = prevEnd - 86_400_000L + 1;
        } else if (periodVal == 1) {
            prevStart = prevEnd - 7L * 86_400_000L + 1;
        } else {
            prevStart = prevEnd - 30L * 86_400_000L + 1;
        }
        double lastPeriod = purchaseDAO.getTotalBetween(prevStart, prevEnd);
        tvLastPeriod.setText(CurrencyFormatter.format(lastPeriod, requireContext()));
    }

    private void loadRecentPurchases() {
        List<Purchase> recent = purchaseDAO.getRecentPurchases(3);
        layoutRecentPurchases.removeAllViews();

        if (recent.isEmpty()) {
            tvHomeEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvHomeEmpty.setVisibility(View.GONE);

        Map<Integer, Category> categoryMap = buildCategoryMap();
        for (Purchase p : recent) {
            View row = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_recent_purchase, layoutRecentPurchases, false);

            ((TextView) row.findViewById(R.id.tv_item_name)).setText(p.getItemName());
            ((TextView) row.findViewById(R.id.tv_date))
                    .setText(dateFormat.format(new Date(p.getDate())));
            ((TextView) row.findViewById(R.id.tv_total_cost))
                    .setText(CurrencyFormatter.format(p.getTotalCost(), requireContext()));

            ImageView icon = row.findViewById(R.id.iv_category_icon);
            Category cat   = categoryMap.get(p.getCategoryId());
            if (cat != null) {
                int resId = requireContext().getResources().getIdentifier(
                        cat.getIconName(), "drawable", requireContext().getPackageName());
                icon.setImageResource(resId > 0 ? resId : R.drawable.ic_category);
            } else {
                icon.setImageResource(R.drawable.ic_category);
            }

            layoutRecentPurchases.addView(row);
        }
    }

    private Map<Integer, Category> buildCategoryMap() {
        Map<Integer, Category> map = new HashMap<>();
        for (Category c : categoryDAO.getAllCategories()) {
            map.put(c.getId(), c);
        }
        return map;
    }
}
