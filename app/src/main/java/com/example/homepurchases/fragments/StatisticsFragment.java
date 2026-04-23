package com.example.homepurchases.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.homepurchases.R;
import com.example.homepurchases.database.CategoryDAO;
import com.example.homepurchases.database.PurchaseDAO;
import com.example.homepurchases.models.Category;
import com.example.homepurchases.utils.CurrencyFormatter;

import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private TextView tvTotalAmount, tvNoData;
    private LinearLayout layoutCategoryBreakdown;

    private PurchaseDAO purchaseDAO;
    private CategoryDAO categoryDAO;

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

        purchaseDAO = new PurchaseDAO(requireContext());
        categoryDAO = new CategoryDAO(requireContext());

        tvTotalAmount           = view.findViewById(R.id.tv_total_amount);
        tvNoData                = view.findViewById(R.id.tv_no_data);
        layoutCategoryBreakdown = view.findViewById(R.id.layout_category_breakdown);

        requireActivity().setTitle(R.string.statistics_title);
        loadStatistics();
    }

    private void loadStatistics() {
        try {
        loadStatisticsInternal();
        } catch (Exception e) {
            Log.e("StatisticsFragment", "loadStatistics failed: " + e.getMessage());
            Toast.makeText(requireContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadStatisticsInternal() {
        double total = purchaseDAO.getTotalExpenses();
        tvTotalAmount.setText(CurrencyFormatter.format(total, requireContext()));

        Map<Integer, Double> expenseMap = purchaseDAO.getExpensesByCategory();
        layoutCategoryBreakdown.removeAllViews();

        if (expenseMap.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            return;
        }
        tvNoData.setVisibility(View.GONE);

        List<Category> allCategories = categoryDAO.getAllCategories();

        for (Map.Entry<Integer, Double> entry : expenseMap.entrySet()) {
            String catName = findCategoryName(allCategories, entry.getKey());
            double amount = entry.getValue();
            int percent = total > 0 ? (int) Math.round((amount / total) * 100) : 0;

            View row = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_stats_category, layoutCategoryBreakdown, false);

            ((TextView)  row.findViewById(R.id.tv_cat_name)).setText(catName);
            ((TextView)  row.findViewById(R.id.tv_cat_amount))
                    .setText(CurrencyFormatter.format(amount, requireContext()));
            ((ProgressBar) row.findViewById(R.id.pb_category)).setProgress(percent);

            layoutCategoryBreakdown.addView(row);
        }
    }

    private String findCategoryName(List<Category> categories, int id) {
        for (Category c : categories) {
            if (c.getId() == id) return c.getName();
        }
        return String.valueOf(id);
    }
}
