package com.example.campusexpense.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.campusexpense.R;
import com.example.campusexpense.data.database.AppDatabase;
import com.example.campusexpense.data.database.BudgetDao;
import com.example.campusexpense.data.database.CategoryDao;
import com.example.campusexpense.data.database.ExpenseDao;
import com.example.campusexpense.data.model.Budget;
import com.example.campusexpense.data.model.Category;
import com.example.campusexpense.data.model.Expense;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private TextView welcomeText, totalExpenseText, currentMonthText;
    // Budget / Top Category Section
    private TextView topCategoryName, topCategoryBudgetText, topCategorySpentText;
    private ProgressBar topCategoryProgressBar;
    private LinearLayout topCategoryCard;

    // Expense Distribution Section
    private LinearLayout distributionContainer;

    private TextView transactionCountText, avgPerDayText, budgetCountText;
    private TextView totalBudgetText, spentText, remainingText;

    private ExpenseDao expenseDao;
    private BudgetDao budgetDao;
    private CategoryDao categoryDao;

    private int userId = -1;
    private String username = "";
    private double Expense = 0.0;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Views
        welcomeText = view.findViewById(R.id.welcomeText);
        totalExpenseText = view.findViewById(R.id.totalExpenseText);
        currentMonthText = view.findViewById(R.id.currentMonthText);

        // Budget / Top Category
        topCategoryCard = view.findViewById(R.id.topCategoryCard);
        topCategoryName = view.findViewById(R.id.topCategoryName);
        topCategoryBudgetText = view.findViewById(R.id.topCategoryBudgetText);
        topCategorySpentText = view.findViewById(R.id.topCategorySpentText);
        topCategoryProgressBar = view.findViewById(R.id.topCategoryProgressBar);

        // Expense Distribution
        distributionContainer = view.findViewById(R.id.distributionContainer);

        transactionCountText = view.findViewById(R.id.transactionCountText);
        avgPerDayText = view.findViewById(R.id.avgPerDayText);
        budgetCountText = view.findViewById(R.id.budgetCountText);

        totalBudgetText = view.findViewById(R.id.totalBudgetText);
        spentText = view.findViewById(R.id.spentText);
        remainingText = view.findViewById(R.id.remainingText);

        // Initialize Database
        AppDatabase db = AppDatabase.getInstance(requireContext());
        expenseDao = db.expenseDao();
        budgetDao = db.budgetDao();
        categoryDao = db.categoryDao();

        // Get User Session
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);
        username = sharedPreferences.getString("username", "User");

        welcomeText.setText("Welcome, " + username);

        if (userId != -1) {
            refreshData();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userId != -1) {
            refreshData();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && userId != -1) {
            refreshData();
        }
    }

    public void refreshData() {
        new Thread(() -> {
            try {
                // Calculate Date Range (Current Month)
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startDate = calendar.getTimeInMillis();

                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                long endDate = calendar.getTimeInMillis() - 1; // End of previous month

                // Fetch General Stats
                Double totalExpenseObj = expenseDao.getTotalExpensesByDateRange(userId, startDate, endDate);
                double totalExpense = (totalExpenseObj != null) ? totalExpenseObj : 0.0;

                int transactionCount = expenseDao.getExpenseCountByDateRange(userId, startDate, endDate);
                Expense avgPerDay = expenseDao.getExpenseById(userId);

                // Calculate Total Budget manually from list to ensure accuracy
                List<Budget> allBudgets = budgetDao.getBudgetByUser(userId);
                double totalBudget = 0.0;
                if (allBudgets != null) {
                    for (Budget b : allBudgets) {
                        totalBudget += b.getAmount();
                    }
                }
                int budgetCount = (allBudgets != null) ? allBudgets.size() : 0;

                double remaining = totalBudget - totalExpense;

                // Find Top Spending Category & Distribution
                List<Expense> expenses = expenseDao.getExpensesByDateRange(userId, startDate, endDate);
                Map<Integer, Double> categoryExpenseMap = new HashMap<>();
                for (Expense e : expenses) {
                    categoryExpenseMap.put(e.getCategoryId(), categoryExpenseMap.getOrDefault(e.getCategoryId(), 0.0) + e.getAmount());
                }

                // Find max expense for Budget Card
                int topCategoryId = -1;
                double maxExpense = 0.0;
                for (Map.Entry<Integer, Double> entry : categoryExpenseMap.entrySet()) {
                    if (entry.getValue() > maxExpense) {
                        maxExpense = entry.getValue();
                        topCategoryId = entry.getKey();
                    }
                }

                // Setup Budget Card Data (Top Category or Latest Budget)
                String categoryName = "None";
                double categoryBudgetAmount = 0.0;
                double categorySpent = maxExpense;

                if (topCategoryId != -1) {
                    Category category = categoryDao.getById(topCategoryId);
                    if (category != null) {
                        categoryName = category.getName();
                    }
                    Budget budget = budgetDao.getBudgetByCategoryAndUser(userId, topCategoryId);
                    if (budget != null) {
                        categoryBudgetAmount = budget.getAmount();
                    }
                } else {
                    // Fallback: No expenses this month, check for any budgets
                    if (allBudgets != null && !allBudgets.isEmpty()) {
                        Budget latestBudget = allBudgets.get(0);
                        topCategoryId = latestBudget.getCategoryId();
                        categoryBudgetAmount = latestBudget.getAmount();
                        categorySpent = 0.0;

                        Category category = categoryDao.getById(topCategoryId);
                        if (category != null) {
                            categoryName = category.getName();
                        }
                    }
                }

                double finalTotalExpense = totalExpense;
                String finalCategoryName = categoryName;
                double finalCategoryBudgetAmount = categoryBudgetAmount;
                double finalCategorySpent = categorySpent;
                double finalAvgPerDay = Expense;
                double finalTotalBudget = totalBudget;
                double finalRemaining = remaining;

                // Prepare distribution data in background
                List<Map.Entry<Integer, Double>> sortedList = new java.util.ArrayList<>(categoryExpenseMap.entrySet());
                sortedList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

                class DistItem {
                    String name;
                    double amount;
                    double percent;
                }
                java.util.List<DistItem> items = new java.util.ArrayList<>();

                for (Map.Entry<Integer, Double> entry : sortedList) {
                    Category cat = categoryDao.getById(entry.getKey());
                    if (cat != null) {
                        DistItem item = new DistItem();
                        item.name = cat.getName();
                        item.amount = entry.getValue();
                        item.percent = (finalTotalExpense > 0) ? (entry.getValue() / finalTotalExpense) * 100 : 0;
                        items.add(item);
                    }
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        totalExpenseText.setText(String.format("$%,.2f", finalTotalExpense));

                        // Top Category / Budget Card
                        if (finalCategoryName.equals("None")) {
                            topCategoryCard.setVisibility(View.GONE);
                        } else {
                            topCategoryCard.setVisibility(View.VISIBLE);
                            topCategoryName.setText(finalCategoryName);
                            topCategoryBudgetText.setText(String.format("Budget: $%,.2f", finalCategoryBudgetAmount));
                            topCategorySpentText.setText(String.format("$%,.2f", finalCategorySpent));

                            double percent = 0;
                            if (finalCategoryBudgetAmount > 0) {
                                percent = (finalCategorySpent / finalCategoryBudgetAmount) * 100;
                                if (percent > 100) percent = 100;
                            }
                            topCategoryProgressBar.setProgress((int) percent);
                        }

                        // Expense Distribution List
                        distributionContainer.removeAllViews();
                        if (items.isEmpty()) {
                            distributionContainer.setVisibility(View.GONE);
                        } else {
                            distributionContainer.setVisibility(View.VISIBLE);
                            LayoutInflater inflater = LayoutInflater.from(getContext());

                            for (DistItem item : items) {
                                View itemView = inflater.inflate(R.layout.item_distribution, distributionContainer, false);
                                TextView catName = itemView.findViewById(R.id.itemCategoryName);
                                TextView dataText = itemView.findViewById(R.id.itemDataText);
                                ProgressBar progressBar = itemView.findViewById(R.id.itemProgressBar);

                                catName.setText(item.name);
                                dataText.setText(String.format("$%,.2f %.1f%%", item.amount, item.percent));
                                progressBar.setProgress((int) item.percent);
                                distributionContainer.addView(itemView);
                            }
                        }

                        // Quick Stats
                        transactionCountText.setText(String.valueOf(transactionCount));
                        avgPerDayText.setText(String.format("$%,.2f", finalAvgPerDay));
                        budgetCountText.setText(String.valueOf(budgetCount));

                        // Monthly Summary
                        totalBudgetText.setText(String.format("$%,.2f", finalTotalBudget));
                        spentText.setText(String.format("$%,.2f", finalTotalExpense));
                        remainingText.setText(String.format("$%,.2f", finalRemaining));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}