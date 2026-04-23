package com.example.homepurchases.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.homepurchases.R;

import java.util.ArrayList;
import java.util.List;

public class FilterCategoryDialog extends DialogFragment {

    public interface OnCategoryFilterListener {
        void onCategorySelected(String categoryName);
        void onFilterCleared();
    }

    private static final String ARG_CATEGORIES = "categories";
    private OnCategoryFilterListener listener;

    public static FilterCategoryDialog newInstance(ArrayList<String> categoryNames) {
        FilterCategoryDialog dialog = new FilterCategoryDialog();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_CATEGORIES, categoryNames);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnCategoryFilterListener(OnCategoryFilterListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ArrayList<String> categories = getArguments() != null
                ? getArguments().getStringArrayList(ARG_CATEGORIES)
                : new ArrayList<>();

        List<String> items = new ArrayList<>();
        items.add(getString(R.string.filter_all));
        if (categories != null) items.addAll(categories);

        String[] itemsArray = items.toArray(new String[0]);

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_by_category)
                .setItems(itemsArray, (dialog, which) -> {
                    if (which == 0) {
                        if (listener != null) listener.onFilterCleared();
                    } else {
                        if (listener != null) listener.onCategorySelected(items.get(which));
                    }
                })
                .create();
    }
}
