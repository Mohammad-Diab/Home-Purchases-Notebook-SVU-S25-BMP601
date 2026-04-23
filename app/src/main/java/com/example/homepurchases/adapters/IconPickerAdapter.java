package com.example.homepurchases.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homepurchases.R;

import java.util.Arrays;
import java.util.List;

public class IconPickerAdapter extends RecyclerView.Adapter<IconPickerAdapter.IconViewHolder> {

    public static final List<String> AVAILABLE_ICONS = Arrays.asList(
            "ic_restaurant",
            "ic_cleaning_services",
            "ic_build",
            "ic_checkroom",
            "ic_devices",
            "ic_local_hospital",
            "ic_sports_esports",
            "ic_receipt",
            "ic_category"
    );

    public interface OnIconSelectedListener {
        void onIconSelected(String iconName);
    }

    private final Context context;
    private final List<String> iconNames;
    private String selectedIconName;
    private final OnIconSelectedListener listener;

    public IconPickerAdapter(Context context,
                             List<String> iconNames,
                             String selectedIconName,
                             OnIconSelectedListener listener) {
        this.context = context;
        this.iconNames = iconNames;
        this.selectedIconName = selectedIconName;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_icon_picker, parent, false);
        return new IconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        String iconName = iconNames.get(position);
        int resId = context.getResources().getIdentifier(
                iconName, "drawable", context.getPackageName());
        holder.ivIcon.setImageResource(resId > 0 ? resId : R.drawable.ic_category);

        boolean selected = iconName.equals(selectedIconName);
        holder.itemView.setAlpha(selected ? 1.0f : 0.45f);
        holder.itemView.setBackgroundResource(
                selected ? R.drawable.bg_accent_swatch : android.R.color.transparent);

        holder.itemView.setOnClickListener(v -> {
            selectedIconName = iconName;
            notifyDataSetChanged();
            if (listener != null) listener.onIconSelected(iconName);
        });
    }

    @Override
    public int getItemCount() {
        return iconNames.size();
    }

    static class IconViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;

        IconViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
        }
    }
}
