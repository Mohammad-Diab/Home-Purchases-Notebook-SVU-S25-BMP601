package com.example.homepurchases.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.homepurchases.MainActivity;
import com.example.homepurchases.R;
import com.example.homepurchases.adapters.CategorySpinnerAdapter;
import com.example.homepurchases.database.CategoryDAO;
import com.example.homepurchases.database.PurchaseDAO;
import com.example.homepurchases.models.Category;
import com.example.homepurchases.models.Purchase;
import com.example.homepurchases.utils.CurrencyFormatter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddEditFragment extends Fragment {

    private TextInputLayout layoutItemName, layoutPrice, layoutQuantity;
    private TextInputEditText etItemName, etPrice, etQuantity, etNotes;
    private Spinner spinnerCategory;
    private TextView tvSelectedDate, tvTotalCost;
    private AppCompatButton btnPickDate, btnSave;

    private PurchaseDAO purchaseDAO;
    private CategoryDAO categoryDAO;
    private List<Category> categories = new ArrayList<>();
    private long selectedDateMillis = System.currentTimeMillis();
    private int purchaseId = -1;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy/MM/dd", new Locale("ar"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        purchaseDAO = new PurchaseDAO(requireContext());
        categoryDAO = new CategoryDAO(requireContext());

        if (getArguments() != null) {
            purchaseId = getArguments().getInt("purchaseId", -1);
        }

        bindViews(view);
        loadCategories();
        tvSelectedDate.setText(CurrencyFormatter.toArabicDigits(dateFormat.format(new Date(selectedDateMillis))));
        setupTotalWatcher();

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> savePurchase());

        if (purchaseId != -1) populateForEdit();
    }

    private void bindViews(View view) {
        layoutItemName  = view.findViewById(R.id.layout_item_name);
        layoutPrice     = view.findViewById(R.id.layout_price);
        layoutQuantity  = view.findViewById(R.id.layout_quantity);
        etItemName      = view.findViewById(R.id.et_item_name);
        etPrice         = view.findViewById(R.id.et_price);
        etQuantity      = view.findViewById(R.id.et_quantity);
        etNotes         = view.findViewById(R.id.et_notes);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        tvSelectedDate  = view.findViewById(R.id.tv_selected_date);
        tvTotalCost     = view.findViewById(R.id.tv_total_cost);
        btnPickDate     = view.findViewById(R.id.btn_pick_date);
        btnSave         = view.findViewById(R.id.btn_save);
    }

    @Override
    public void onResume() {
        super.onResume();
        int prevCategoryId = -1;
        int pos = spinnerCategory.getSelectedItemPosition();
        if (pos >= 0 && pos < categories.size()) {
            prevCategoryId = categories.get(pos).getId();
        }
        loadCategories();
        if (prevCategoryId != -1) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == prevCategoryId) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
    }

    private void loadCategories() {
        categories = categoryDAO.getAllCategories();
        List<String> names = new ArrayList<>();
        for (Category c : categories) names.add(c.getName());
        spinnerCategory.setAdapter(
                new CategorySpinnerAdapter(requireContext(), names));
    }

    private void populateForEdit() {
        Purchase p = purchaseDAO.getPurchaseById(purchaseId);
        if (p == null) return;

        etItemName.setText(p.getItemName());
        double displayPrice = CurrencyFormatter.toDisplayAmount(p.getPrice(), requireContext());
        etPrice.setText(String.valueOf(displayPrice));
        etQuantity.setText(String.valueOf(p.getQuantity()));
        if (p.getNotes() != null) etNotes.setText(p.getNotes());

        selectedDateMillis = p.getDate();
        tvSelectedDate.setText(CurrencyFormatter.toArabicDigits(dateFormat.format(new Date(selectedDateMillis))));

        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == p.getCategoryId()) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
        updateTotalCost();
    }

    private void setupTotalWatcher() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { updateTotalCost(); }
        };
        etPrice.addTextChangedListener(watcher);
        etQuantity.addTextChangedListener(watcher);
    }

    private void updateTotalCost() {
        try {
            double price = Double.parseDouble(etPrice.getText().toString());
            int qty = Integer.parseInt(etQuantity.getText().toString());
            tvTotalCost.setText(CurrencyFormatter.format(
                    CurrencyFormatter.toStorageAmount(price, requireContext()) * qty,
                    requireContext()));
        } catch (NumberFormatException e) {
            tvTotalCost.setText("");
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedDateMillis);
        new DatePickerDialog(requireContext(), (dp, year, month, day) -> {
            cal.set(year, month, day);
            selectedDateMillis = cal.getTimeInMillis();
            tvSelectedDate.setText(CurrencyFormatter.toArabicDigits(dateFormat.format(new Date(selectedDateMillis))));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void savePurchase() {
        String name = etItemName.getText().toString().trim();
        if (name.isEmpty()) {
            layoutItemName.setError(getString(R.string.error_item_name_required));
            return;
        }
        layoutItemName.setError(null);

        double priceDisplay;
        try {
            priceDisplay = Double.parseDouble(etPrice.getText().toString().trim());
            if (priceDisplay <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            layoutPrice.setError(getString(R.string.error_price_invalid));
            return;
        }
        layoutPrice.setError(null);

        int quantity;
        try {
            quantity = Integer.parseInt(etQuantity.getText().toString().trim());
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            layoutQuantity.setError(getString(R.string.error_quantity_invalid));
            return;
        }
        layoutQuantity.setError(null);

        if (categories.isEmpty()) {
            Toast.makeText(requireContext(),
                    R.string.error_category_required, Toast.LENGTH_SHORT).show();
            return;
        }

        Category cat = categories.get(spinnerCategory.getSelectedItemPosition());
        double priceInNewSP = CurrencyFormatter.toStorageAmount(priceDisplay, requireContext());
        String notes = etNotes.getText().toString().trim();

        Purchase purchase = new Purchase(
                purchaseId == -1 ? 0 : purchaseId,
                name,
                cat.getId(),
                priceInNewSP,
                quantity,
                priceInNewSP * quantity,
                selectedDateMillis,
                notes.isEmpty() ? null : notes
        );

        if (purchaseId == -1) {
            purchaseDAO.insertPurchase(purchase);
            Toast.makeText(requireContext(), R.string.purchase_added, Toast.LENGTH_SHORT).show();
        } else {
            purchaseDAO.updatePurchase(purchase);
            Toast.makeText(requireContext(), R.string.purchase_updated, Toast.LENGTH_SHORT).show();
        }

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getSoundManager().playSave();
        }

        Navigation.findNavController(requireView()).navigateUp();
    }
}
