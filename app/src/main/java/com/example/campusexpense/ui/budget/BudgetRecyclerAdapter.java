package com.example.campusexpense.ui.budget;

import android.icu.text.NumberFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpense.R;
import com.example.campusexpense.data.model.Budget;

import java.util.List;
import java.util.Locale;

public class BudgetRecyclerAdapter extends RecyclerView.Adapter<BudgetRecyclerAdapter.ViewHolder> {
    private List<Budget> budgetList;
    private List<String> categoryNames;
    private OnEditClickListener OnEditClickListener;
    private OnDeleteClickListener OnDeleteClickListener;

    public interface OnEditClickListener {
        void onEditClick(Budget budget);
    }
    public interface OnDeleteClickListener {
        void onDeleteClick(Budget budget);
    }
    @Override
    public int getItemCount() {return budgetList.size();}
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameText;
        TextView periodText;
        TextView amountText;
        ProgressBar progressBar;
        TextView progressText;
        ImageButton editButton;
        ImageButton deleteButton;

        ViewHolder(@Nullable View itemView) {
            super(itemView);
            categoryNameText = itemView.findViewById(R.id.categoryNameText);
            periodText = itemView.findViewById(R.id.periodText);
            amountText = itemView.findViewById(R.id.amountText);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressText = itemView.findViewById(R.id.progressText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    public BudgetRecyclerAdapter(List<Budget> budgetList, List<String> categoryNames,
                                 OnEditClickListener onEditClickListener,
                                 OnDeleteClickListener onDeleteClickListener) {
        this.budgetList = budgetList;
        this.categoryNames = categoryNames;
        this.OnEditClickListener = onEditClickListener;
        this.OnDeleteClickListener = onDeleteClickListener;

    }

    @Nullable
    @Override
    public ViewHolder onCreateViewHolder(@Nullable ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget_card, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@Nullable ViewHolder holder, int position) {
        Budget budget = budgetList.get(position);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        String categoryName = (position < categoryNames.size()) ? categoryNames.get(position) : "Unknown";
        holder.categoryNameText.setText(categoryName);
        holder.periodText.setText(budget.getPeriod());
        holder.amountText.setText(currencyFormat.format(budget.getAmount()));
        double spent=0.0;
        double percentage = (budget.getAmount() > 0) ? (spent / budget.getAmount()) * 100 : 0;
        int progress = (int) Math.min(Math.max(percentage, 0), 100);
        holder.progressBar.setProgress(progress);
        holder.progressText.setText(String.format(Locale.getDefault(), "%.0f%% used", percentage));
        holder.editButton.setOnClickListener(v -> OnEditClickListener.onEditClick(budget));
        holder.deleteButton.setOnClickListener(v -> OnDeleteClickListener.onDeleteClick(budget));


    }

}
