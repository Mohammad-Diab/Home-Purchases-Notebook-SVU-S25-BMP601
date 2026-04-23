package com.example.homepurchases.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homepurchases.R;
import com.example.homepurchases.models.Category;
import com.example.homepurchases.models.Purchase;
import com.example.homepurchases.utils.CurrencyFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder> {

    public interface OnPurchaseActionListener {
        void onItemClick(Purchase purchase);
        void onDeleteClick(Purchase purchase);
    }

    private final Context context;
    private List<Purchase> purchases = new ArrayList<>();
    private Map<Integer, Category> categoryMap;
    private final OnPurchaseActionListener listener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy/MM/dd", new Locale("ar"));

    public PurchaseAdapter(Context context,
                           Map<Integer, Category> categoryMap,
                           OnPurchaseActionListener listener) {
        this.context = context;
        this.categoryMap = categoryMap;
        this.listener = listener;
    }

    public void updateList(List<Purchase> newList) {
        purchases = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateCategoryMap(Map<Integer, Category> newMap) {
        categoryMap = newMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PurchaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_purchase, parent, false);
        return new PurchaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchaseViewHolder holder, int position) {
        Purchase purchase = purchases.get(position);
        Category category = categoryMap != null
                ? categoryMap.get(purchase.getCategoryId())
                : null;

        holder.tvItemName.setText(purchase.getItemName());
        holder.tvTotalCost.setText(CurrencyFormatter.format(purchase.getTotalCost(), context));
        holder.tvDate.setText(dateFormat.format(new Date(purchase.getDate())));

        if (category != null) {
            holder.tvCategoryName.setText(category.getName());
            int resId = context.getResources().getIdentifier(
                    category.getIconName(), "drawable", context.getPackageName());
            holder.ivCategoryIcon.setImageResource(resId > 0 ? resId : R.drawable.ic_category);
        } else {
            holder.tvCategoryName.setText("");
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_category);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(purchase);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(purchase);
        });
    }

    @Override
    public int getItemCount() {
        return purchases.size();
    }

    static class PurchaseViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivCategoryIcon;
        final TextView tvItemName, tvCategoryName, tvTotalCost, tvDate;
        final ImageButton btnDelete;

        PurchaseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvTotalCost = itemView.findViewById(R.id.tv_total_cost);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
