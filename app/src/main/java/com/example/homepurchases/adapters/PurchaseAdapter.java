package com.example.homepurchases.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
        holder.tvDate.setText(CurrencyFormatter.toArabicDigits(
                dateFormat.format(new Date(purchase.getDate()))));

        if (category != null) {
            holder.tvCategoryName.setText(category.getName());
            int resId = context.getResources().getIdentifier(
                    category.getIconName(), "drawable", context.getPackageName());
            holder.ivCategoryIcon.setImageResource(resId > 0 ? resId : R.drawable.ic_category);
        } else {
            holder.tvCategoryName.setText("");
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_category);
        }

        // Reset swipe state on every bind (handles recycled views)
        holder.cardFront.animate().cancel();
        holder.cardFront.setTranslationX(0);

        final float actionsWidthPx = 112 * context.getResources().getDisplayMetrics().density;
        attachSwipe(holder, actionsWidthPx, purchase);

        holder.btnEdit.setOnClickListener(v -> {
            holder.cardFront.animate().translationX(0).setDuration(150).start();
            if (listener != null) listener.onItemClick(purchase);
        });

        holder.btnDelete.setOnClickListener(v -> {
            holder.cardFront.animate().translationX(0).setDuration(150).start();
            if (listener != null) listener.onDeleteClick(purchase);
        });
    }

    private void attachSwipe(PurchaseViewHolder holder, float maxOffset, Purchase purchase) {
        final float[] startRawX = {0};
        final float[] startTransX = {0};
        final boolean[] swiping = {false};
        final float threshold = 10 * context.getResources().getDisplayMetrics().density;

        holder.cardFront.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startRawX[0] = event.getRawX();
                    startTransX[0] = holder.cardFront.getTranslationX();
                    swiping[0] = false;
                    return true;

                case MotionEvent.ACTION_MOVE: {
                    float dx = event.getRawX() - startRawX[0];
                    if (!swiping[0] && Math.abs(dx) > threshold) swiping[0] = true;
                    if (swiping[0]) {
                        float newTx = startTransX[0] + dx;
                        newTx = Math.max(0f, Math.min(maxOffset, newTx));
                        holder.cardFront.setTranslationX(newTx);
                    }
                    return true;
                }

                case MotionEvent.ACTION_UP: {
                    if (swiping[0]) {
                        float tx = holder.cardFront.getTranslationX();
                        if (tx > maxOffset / 2) {
                            holder.cardFront.animate().translationX(maxOffset).setDuration(150).start();
                        } else {
                            holder.cardFront.animate().translationX(0).setDuration(150).start();
                        }
                    } else {
                        if (holder.cardFront.getTranslationX() > 0) {
                            holder.cardFront.animate().translationX(0).setDuration(150).start();
                        }
                    }
                    return true;
                }

                case MotionEvent.ACTION_CANCEL:
                    holder.cardFront.animate().translationX(0).setDuration(150).start();
                    return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return purchases.size();
    }

    static class PurchaseViewHolder extends RecyclerView.ViewHolder {
        final CardView cardFront;
        final ImageView ivCategoryIcon;
        final TextView tvItemName, tvCategoryName, tvTotalCost, tvDate;
        final ImageButton btnEdit, btnDelete;

        PurchaseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardFront       = itemView.findViewById(R.id.card_front);
            ivCategoryIcon  = itemView.findViewById(R.id.iv_category_icon);
            tvItemName      = itemView.findViewById(R.id.tv_item_name);
            tvCategoryName  = itemView.findViewById(R.id.tv_category_name);
            tvTotalCost     = itemView.findViewById(R.id.tv_total_cost);
            tvDate          = itemView.findViewById(R.id.tv_date);
            btnEdit         = itemView.findViewById(R.id.btn_edit);
            btnDelete       = itemView.findViewById(R.id.btn_delete);
        }
    }
}
