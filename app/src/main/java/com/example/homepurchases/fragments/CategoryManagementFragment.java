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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homepurchases.R;
import com.example.homepurchases.adapters.CategoryManagementAdapter;
import com.example.homepurchases.adapters.IconPickerAdapter;
import com.example.homepurchases.database.CategoryDAO;
import com.example.homepurchases.models.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class CategoryManagementFragment extends Fragment
        implements CategoryManagementAdapter.OnCategoryActionListener {

    private RecyclerView rvCategories;
    private TextView tvEmptyCategories;
    private CategoryManagementAdapter adapter;
    private CategoryDAO categoryDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categoryDAO = new CategoryDAO(requireContext());
        rvCategories       = view.findViewById(R.id.rv_categories);
        tvEmptyCategories  = view.findViewById(R.id.tv_empty_categories);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_category);

        requireActivity().setTitle(R.string.categories_title);

        adapter = new CategoryManagementAdapter(requireContext(), this);
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategories.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showCategoryDialog(null));
        loadCategories();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCategories();
    }

    private void loadCategories() {
        List<Category> list = categoryDAO.getAllCategories();
        adapter.updateList(list);
        tvEmptyCategories.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEditClick(Category category) {
        showCategoryDialog(category);
    }

    @Override
    public void onDeleteClick(Category category) {
        if (categoryDAO.hasPurchases(category.getId())) {
            Toast.makeText(requireContext(),
                    R.string.category_has_purchases, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.btn_confirm, (d, w) -> {
                    categoryDAO.deleteCategory(category.getId());
                    Toast.makeText(requireContext(),
                            R.string.category_deleted, Toast.LENGTH_SHORT).show();
                    loadCategories();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showCategoryDialog(@Nullable Category existing) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_category, null);

        TextInputLayout layoutName = dialogView.findViewById(R.id.layout_cat_name);
        TextInputEditText etName   = dialogView.findViewById(R.id.et_cat_name);
        TextInputEditText etDesc   = dialogView.findViewById(R.id.et_cat_desc);
        RecyclerView rvIcons       = dialogView.findViewById(R.id.rv_icon_picker);

        // Pre-fill for edit
        String selectedIcon = existing != null ? existing.getIconName() : "ic_category";
        if (existing != null) {
            etName.setText(existing.getName());
            if (existing.getDescription() != null) etDesc.setText(existing.getDescription());
        }

        // Icon picker
        final String[] pickedIcon = {selectedIcon};
        IconPickerAdapter iconAdapter = new IconPickerAdapter(
                requireContext(),
                IconPickerAdapter.AVAILABLE_ICONS,
                selectedIcon,
                iconName -> pickedIcon[0] = iconName);
        rvIcons.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvIcons.setAdapter(iconAdapter);

        int title = existing == null ? R.string.add_category : R.string.edit_category;
        int confirmMsg = existing == null ? R.string.category_added : R.string.category_updated;

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(R.string.btn_save, (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        layoutName.setError(getString(R.string.label_category_name));
                        return;
                    }
                    String desc = etDesc.getText().toString().trim();
                    Category category = new Category(
                            existing == null ? 0 : existing.getId(),
                            name,
                            pickedIcon[0],
                            desc.isEmpty() ? null : desc);

                    if (existing == null) {
                        categoryDAO.insertCategory(category);
                    } else {
                        categoryDAO.updateCategory(category);
                    }
                    Toast.makeText(requireContext(), confirmMsg, Toast.LENGTH_SHORT).show();
                    loadCategories();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
