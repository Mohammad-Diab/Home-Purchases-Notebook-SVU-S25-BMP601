package com.example.homepurchases.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.HorizontalScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.homepurchases.views.BackspaceEditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homepurchases.MainActivity;
import com.example.homepurchases.R;
import com.example.homepurchases.adapters.PurchaseAdapter;
import com.example.homepurchases.database.CategoryDAO;
import com.example.homepurchases.database.PurchaseDAO;
import com.example.homepurchases.dialogs.FilterCategoryDialog;
import com.example.homepurchases.dialogs.FilterDateDialog;
import com.example.homepurchases.models.Category;
import com.example.homepurchases.models.Purchase;
import com.example.homepurchases.utils.CurrencyFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PurchasesFragment extends Fragment
        implements PurchaseAdapter.OnPurchaseActionListener {

    private RecyclerView rvPurchases;
    private TextView tvEmptyPurchases, tvEmptyFiltered;
    private LinearLayout pillsContainer;
    private BackspaceEditText etSearch;
    private ImageButton btnFilter;
    private ImageButton btnClearSearch;
    private PurchaseAdapter adapter;
    private PurchaseDAO purchaseDAO;
    private CategoryDAO categoryDAO;

    // Filter state — all three stack independently
    private int filteredCategoryId = -1;
    private String filteredCategoryName = null;
    private long filterStart = -1, filterEnd = -1;
    private String filterDateLabel = null;
    private String searchQuery = "";

    private boolean isClearing = false;

    private final SimpleDateFormat pillDateFmt =
            new SimpleDateFormat("yyyy/MM/dd", new Locale("ar"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchases, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isClearing = true; // block TextWatcher until onViewStateRestored

        purchaseDAO      = new PurchaseDAO(requireContext());
        categoryDAO      = new CategoryDAO(requireContext());
        rvPurchases      = view.findViewById(R.id.rv_purchases);
        tvEmptyPurchases = view.findViewById(R.id.tv_empty_purchases);
        tvEmptyFiltered  = view.findViewById(R.id.tv_empty_filtered);
        pillsContainer   = view.findViewById(R.id.pills_container);
        etSearch         = view.findViewById(R.id.et_search);
        btnFilter        = view.findViewById(R.id.btn_filter);
        btnClearSearch   = view.findViewById(R.id.btn_clear_search);

        btnClearSearch.setOnClickListener(v -> clearAllFilters());
        etSearch.setOnBackspaceEmptyListener(this::removeLastFilter);

        requireActivity().setTitle(R.string.tab_purchases);

        // Outlined rounded background on the container that holds scroll + clear button
        View searchContainer = view.findViewById(R.id.search_input_container);
        int borderColor = obtainAttrColor(android.R.attr.textColorSecondary);
        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setShape(GradientDrawable.RECTANGLE);
        searchBg.setCornerRadius(dpToPx(24));
        searchBg.setStroke(dpToPx(1), borderColor);
        searchBg.setColor(Color.TRANSPARENT);
        searchContainer.setBackground(searchBg);
        searchContainer.setClipToOutline(true);

        adapter = new PurchaseAdapter(requireContext(), buildCategoryMap(), this);
        rvPurchases.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPurchases.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (isClearing) return;
                searchQuery = s.toString().trim();
                updatePills();
                loadPurchases();
            }
        });

        btnFilter.setOnClickListener(v -> showFilterMenu());

        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        fab.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_purchases_to_addEdit));

        updatePills();
        loadPurchases();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        isClearing = false; // view state fully restored — TextWatcher can respond to real input now
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.updateCategoryMap(buildCategoryMap());
        loadPurchases();
    }

    private Map<Integer, Category> buildCategoryMap() {
        Map<Integer, Category> map = new HashMap<>();
        for (Category c : categoryDAO.getAllCategories()) {
            map.put(c.getId(), c);
        }
        return map;
    }

    private void loadPurchases() {
        try {
            Integer catId = filteredCategoryId != -1 ? filteredCategoryId : null;
            Long    start = filterStart != -1 ? filterStart : null;
            Long    end   = filterEnd   != -1 ? filterEnd   : null;
            String  query = !searchQuery.isEmpty() ? searchQuery : null;

            List<Purchase> list = purchaseDAO.getFilteredPurchases(catId, start, end, query);
            adapter.updateList(list);

            boolean hasFilter = catId != null || start != null || query != null;
            tvEmptyPurchases.setVisibility(!hasFilter && list.isEmpty() ? View.VISIBLE : View.GONE);
            tvEmptyFiltered.setVisibility(hasFilter && list.isEmpty() ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            Log.e("PurchasesFragment", "loadPurchases failed: " + e.getMessage());
            Toast.makeText(requireContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePills() {
        pillsContainer.removeAllViews();

        if (filteredCategoryId != -1 && filteredCategoryName != null) {
            addPill(getString(R.string.filter_pill_category, filteredCategoryName), () -> {
                filteredCategoryId = -1;
                filteredCategoryName = null;
                updatePills();
                loadPurchases();
            });
        }

        if (filterStart != -1 && filterDateLabel != null) {
            addPill(filterDateLabel, () -> {
                filterStart = -1;
                filterEnd = -1;
                filterDateLabel = null;
                updatePills();
                loadPurchases();
            });
        }

        boolean anyActive = filteredCategoryId != -1 || filterStart != -1 || !searchQuery.isEmpty();

        // Filter button tint: colorPrimary when any filter active
        int tintColor = anyActive
                ? obtainAttrColor(com.google.android.material.R.attr.colorPrimary)
                : obtainAttrColor(android.R.attr.textColorSecondary);
        ImageViewCompat.setImageTintList(btnFilter, ColorStateList.valueOf(tintColor));

        // Clear button visibility
        if (btnClearSearch != null) {
            btnClearSearch.setVisibility(anyActive ? View.VISIBLE : View.GONE);
        }
    }

    private void removeLastFilter() {
        // Pop filters in reverse display order: date → category
        if (filterStart != -1) {
            filterStart = -1;
            filterEnd = -1;
            filterDateLabel = null;
        } else if (filteredCategoryId != -1) {
            filteredCategoryId = -1;
            filteredCategoryName = null;
        }
        updatePills();
        loadPurchases();
    }

    private void addPill(String label, Runnable onClose) {
        View pill = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_filter_pill, pillsContainer, false);
        ((TextView) pill.findViewById(R.id.tv_pill_label)).setText(label);

        ImageButton closeBtn = pill.findViewById(R.id.btn_pill_close);
        int secondaryColor = obtainAttrColor(android.R.attr.textColorSecondary);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        int r = Color.red(secondaryColor);
        int g = Color.green(secondaryColor);
        int b = Color.blue(secondaryColor);
        circle.setColor(Color.argb(160, r, g, b));

        GradientDrawable mask = new GradientDrawable();
        mask.setShape(GradientDrawable.OVAL);
        mask.setColor(Color.WHITE);

        int rippleColor = obtainAttrColor(android.R.attr.colorControlHighlight);
        android.graphics.drawable.RippleDrawable ripple =
                new android.graphics.drawable.RippleDrawable(
                        ColorStateList.valueOf(rippleColor), circle, mask);
        closeBtn.setBackground(ripple);
        ImageViewCompat.setImageTintList(closeBtn, ColorStateList.valueOf(Color.WHITE));

        closeBtn.setOnClickListener(v -> onClose.run());
        pillsContainer.addView(pill);
    }

    private void showFilterMenu() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getSoundManager().playOpen();
        }
        String[] options = {
                getString(R.string.filter_by_category),
                getString(R.string.filter_by_date),
                getString(R.string.filter_clear_all)
        };
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showCategoryFilter();
                    else if (which == 1) showDateFilter();
                    else clearAllFilters();
                })
                .show();
    }

    private void showCategoryFilter() {
        Set<Integer> usedIds = purchaseDAO.getCategoryIdsWithPurchases();
        List<Category> allCategories = categoryDAO.getAllCategories();

        List<Category> categories = new ArrayList<>();
        for (Category c : allCategories) {
            if (usedIds.contains(c.getId())) categories.add(c);
        }

        ArrayList<String> names = new ArrayList<>();
        for (Category c : categories) names.add(c.getName());

        FilterCategoryDialog dialog = FilterCategoryDialog.newInstance(names);
        dialog.setOnCategoryFilterListener(new FilterCategoryDialog.OnCategoryFilterListener() {
            @Override
            public void onCategorySelected(String categoryName) {
                for (Category c : categories) {
                    if (c.getName().equals(categoryName)) {
                        filteredCategoryId = c.getId();
                        filteredCategoryName = c.getName();
                        break;
                    }
                }
                updatePills();
                loadPurchases();
            }
            @Override
            public void onFilterCleared() { clearAllFilters(); }
        });
        dialog.show(getChildFragmentManager(), "filter_category");
    }

    private void showDateFilter() {
        long earliest = purchaseDAO.getEarliestPurchaseDate();
        long latest   = purchaseDAO.getLatestPurchaseDate();
        long today    = System.currentTimeMillis();
        long maxDate  = Math.max(today, latest != -1 ? latest : today);

        FilterDateDialog dialog = FilterDateDialog.newInstance(
                earliest != -1 ? earliest : 0,
                maxDate);
        dialog.setOnDateFilterListener(new FilterDateDialog.OnDateFilterListener() {
            @Override
            public void onDateRangeSelected(long startMillis, long endMillis) {
                filterStart = startMillis;
                filterEnd   = endMillis;
                String from = CurrencyFormatter.toArabicDigits(
                        pillDateFmt.format(new Date(startMillis)));
                String to = CurrencyFormatter.toArabicDigits(
                        pillDateFmt.format(new Date(endMillis)));
                filterDateLabel = getString(R.string.filter_pill_date, from, to);
                updatePills();
                loadPurchases();
            }
            @Override
            public void onFilterCleared() { clearAllFilters(); }
        });
        dialog.show(getChildFragmentManager(), "filter_date");
    }

    private void clearAllFilters() {
        filteredCategoryId = -1;
        filteredCategoryName = null;
        filterStart = -1;
        filterEnd   = -1;
        filterDateLabel = null;
        searchQuery = "";
        isClearing = true;
        etSearch.setText("");
        isClearing = false;
        updatePills();
        loadPurchases();
    }

    @Override
    public void onItemClick(Purchase purchase) {
        Bundle args = new Bundle();
        args.putInt("purchaseId", purchase.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_purchases_to_addEdit, args);
    }

    @Override
    public void onDeleteClick(Purchase purchase) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getSoundManager().playConfirm();
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.btn_confirm, (d, w) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).getSoundManager().playDelete();
                    }
                    purchaseDAO.deletePurchase(purchase.getId());
                    Toast.makeText(requireContext(),
                            R.string.purchase_deleted, Toast.LENGTH_SHORT).show();
                    loadPurchases();
                })
                .setNegativeButton(R.string.btn_cancel, (d, w) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).getSoundManager().playCancel();
                    }
                })
                .show();
    }

    private int obtainAttrColor(int attr) {
        int[] attrs = {attr};
        android.content.res.TypedArray ta = requireContext().obtainStyledAttributes(attrs);
        int color = ta.getColor(0, 0xFF808080);
        ta.recycle();
        return color;
    }

    private int dpToPx(float dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }
}
