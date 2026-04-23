package com.example.homepurchases.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.homepurchases.R;

import java.util.Calendar;

public class FilterDateDialog extends DialogFragment {

    public interface OnDateFilterListener {
        void onDateRangeSelected(long startMillis, long endMillis);
        void onFilterCleared();
    }

    private OnDateFilterListener listener;

    public void setOnDateFilterListener(OnDateFilterListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_filter_date, null);

        DatePicker dpFrom = view.findViewById(R.id.dp_from);
        DatePicker dpTo = view.findViewById(R.id.dp_to);

        view.findViewById(R.id.btn_apply_filter).setOnClickListener(v -> {
            Calendar calFrom = Calendar.getInstance();
            calFrom.set(dpFrom.getYear(), dpFrom.getMonth(), dpFrom.getDayOfMonth(), 0, 0, 0);
            calFrom.set(Calendar.MILLISECOND, 0);

            Calendar calTo = Calendar.getInstance();
            calTo.set(dpTo.getYear(), dpTo.getMonth(), dpTo.getDayOfMonth(), 23, 59, 59);
            calTo.set(Calendar.MILLISECOND, 999);

            if (calFrom.after(calTo)) {
                dpFrom.setVisibility(View.VISIBLE);
                return;
            }

            if (listener != null) {
                listener.onDateRangeSelected(calFrom.getTimeInMillis(), calTo.getTimeInMillis());
            }
            dismiss();
        });

        view.findViewById(R.id.btn_clear_filter).setOnClickListener(v -> {
            if (listener != null) listener.onFilterCleared();
            dismiss();
        });

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_by_date)
                .setView(view)
                .create();
    }
}
