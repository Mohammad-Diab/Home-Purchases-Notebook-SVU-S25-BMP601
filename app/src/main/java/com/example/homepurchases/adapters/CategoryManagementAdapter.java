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

import java.util.ArrayList;
import java.util.List;

public class CategoryManagementAdapter extends RecyclerView.Adapter<CategoryManagementAdapter.CategoryViewHolder> {

    public interface OnCategoryActionListener {
        void onEditClick(Category category);
        void onDeleteClick(Category category);
    }

    private final Context context;
    private List<Category> categories = new ArrayList<>();
    private final OnCategoryActionListener listener;

    public CategoryManagementAdapter(Context context, OnCategoryActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void updateList(List<Category> newList) {
        categories = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_category_manage, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);

        holder.tvCategoryName.setText(category.getName());
        String desc = category.getDescription();
        holder.tvCategoryDescription.setText(desc != null ? desc : "");

        int resId = context.getResources().getIdentifier(
                category.getIconName(), "drawable", context.getPackageName());
        holder.ivCategoryIcon.setImageResource(resId > 0 ? resId : R.drawable.ic_category);

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(category);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(category);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivCategoryIcon;
        final TextView tvCategoryName, tvCategoryDescription;
        final ImageButton btnEdit, btnDelete;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvCategoryDescription = itemView.findViewById(R.id.tv_category_description);
            btnEdit = itemView.findViewById(R.id.btn_edit_category);
            btnDelete = itemView.findViewById(R.id.btn_delete_category);
        }
    }
}
