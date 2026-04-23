package com.example.homepurchases.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchasesFragment extends Fragment
        implements PurchaseAdapter.OnPurchaseActionListener {

    private RecyclerView rvPurchases;
    private TextView tvEmptyPurchases, tvEmptyFiltered;
    private PurchaseAdapter adapter;
    private PurchaseDAO purchaseDAO;
    private CategoryDAO categoryDAO;

    private enum FilterMode { NONE, CATEGORY, DATE }
    private FilterMode filterMode = FilterMode.NONE;
    private int filteredCategoryId = -1;
    private long filterStart, filterEnd;
    private String searchQuery = "";

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

        purchaseDAO      = new PurchaseDAO(requireContext());
        categoryDAO      = new CategoryDAO(requireContext());
        rvPurchases      = view.findViewById(R.id.rv_purchases);
        tvEmptyPurchases = view.findViewById(R.id.tv_empty_purchases);
        tvEmptyFiltered  = view.findViewById(R.id.tv_empty_filtered);

        requireActivity().setTitle(R.string.tab_purchases);

        adapter = new PurchaseAdapter(requireContext(), buildCategoryMap(), this);
        rvPurchases.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPurchases.setAdapter(adapter);

        SearchView searchView = view.findViewById(R.id.search_purchases);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                searchQuery = newText.trim();
                loadPurchases();
                return true;
            }
        });

        view.findViewById(R.id.btn_filter).setOnClickListener(v -> showFilterMenu());

        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).getSoundManager().playFab();
            }
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_purchases_to_addEdit);
        });

        loadPurchases();
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
        List<Purchase> list;
        if (!searchQuery.isEmpty()) {
            list = purchaseDAO.searchPurchases(searchQuery);
        } else if (filterMode == FilterMode.CATEGORY) {
            list = purchaseDAO.getPurchasesByCategory(filteredCategoryId);
        } else if (filterMode == FilterMode.DATE) {
            list = purchaseDAO.getPurchasesByDateRange(filterStart, filterEnd);
        } else {
            list = purchaseDAO.getAllPurchases();
        }

        adapter.updateList(list);

        boolean isFiltered = !searchQuery.isEmpty() || filterMode != FilterMode.NONE;
        tvEmptyPurchases.setVisibility((!isFiltered && list.isEmpty()) ? View.VISIBLE : View.GONE);
        tvEmptyFiltered.setVisibility((isFiltered && list.isEmpty()) ? View.VISIBLE : View.GONE);
    }

    private void showFilterMenu() {
        String[] options = {
                getString(R.string.filter_by_category),
                getString(R.string.filter_by_date),
                getString(R.string.filter_clear)
        };
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showCategoryFilter();
                    else if (which == 1) showDateFilter();
                    else clearFilter();
                })
                .show();
    }

    private void showCategoryFilter() {
        List<Category> categories = categoryDAO.getAllCategories();
        ArrayList<String> names = new ArrayList<>();
        for (Category c : categories) names.add(c.getName());

        FilterCategoryDialog dialog = FilterCategoryDialog.newInstance(names);
        dialog.setOnCategoryFilterListener(new FilterCategoryDialog.OnCategoryFilterListener() {
            @Override
            public void onCategorySelected(String categoryName) {
                for (Category c : categories) {
                    if (c.getName().equals(categoryName)) {
                        filteredCategoryId = c.getId();
                        break;
                    }
                }
                filterMode = FilterMode.CATEGORY;
                loadPurchases();
            }
            @Override
            public void onFilterCleared() { clearFilter(); }
        });
        dialog.show(getChildFragmentManager(), "filter_category");
    }

    private void showDateFilter() {
        FilterDateDialog dialog = new FilterDateDialog();
        dialog.setOnDateFilterListener(new FilterDateDialog.OnDateFilterListener() {
            @Override
            public void onDateRangeSelected(long startMillis, long endMillis) {
                filterMode = FilterMode.DATE;
                filterStart = startMillis;
                filterEnd = endMillis;
                loadPurchases();
            }
            @Override
            public void onFilterCleared() { clearFilter(); }
        });
        dialog.show(getChildFragmentManager(), "filter_date");
    }

    private void clearFilter() {
        filterMode = FilterMode.NONE;
        filteredCategoryId = -1;
        loadPurchases();
    }

    @Override
    public void onItemClick(Purchase purchase) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getSoundManager().playClick();
        }
        Bundle args = new Bundle();
        args.putInt("purchaseId", purchase.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_purchases_to_addEdit, args);
    }

    @Override
    public void onDeleteClick(Purchase purchase) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.btn_confirm, (d, w) -> {
                    purchaseDAO.deletePurchase(purchase.getId());
                    Toast.makeText(requireContext(),
                            R.string.purchase_deleted, Toast.LENGTH_SHORT).show();
                    loadPurchases();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
