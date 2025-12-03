package com.example.campusexpense.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpense.R;
import com.example.campusexpense.data.model.Category;


import java.util.List;

public class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryRecyclerAdapter.ViewHolder> {
    private List<Category> categories;
    private onEditCategoryListener onEditClick;
    private onDeleteCategoryListener onDeleteClick;

    public interface onEditCategoryListener {
        void onEditCategory(Category category);
    }

    public interface onDeleteCategoryListener {
        void onDeleteCategory(Category category);
    }

    public CategoryRecyclerAdapter(List<Category> categories, onEditCategoryListener onEditClick,
                                   onDeleteCategoryListener onDeleteClick) {
        this.categories = categories;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    @Nullable
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_card,
                parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.nameText.setText(category.getName());
        holder.editButton.setOnClickListener(v -> onEditClick.onEditCategory(category));
        holder.deleteButton.setOnClickListener(v -> onDeleteClick.onDeleteCategory(category));
    }

    @Override
    public int getItemCount() {return categories.size();}

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        ImageButton editButton;
        ImageButton deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
