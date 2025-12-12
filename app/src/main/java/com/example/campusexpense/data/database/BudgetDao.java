package com.example.campusexpense.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.campusexpense.data.model.Budget;

import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    long insert(Budget budget);
    @Update
    void update(Budget budget);
    
    @Delete
    void delete(Budget budget);
    @Query("SELECT * FROM budgets WHERE UserId = :userId ORDER BY createdAt DESC")
    List<Budget> getBudgetByUser(int userId);
    @Query("SELECT * FROM budgets WHERE UserId = :userId AND categoryId= :categoryId")
    Budget getBudgetByCategoryAndUser(int userId, int categoryId);
    @Query("SELECT * FROM budgets WHERE id = :id")
    Budget getBydId(int id);
    @Query("SELECT COUNT(*) FROM budgets WHERE UserId = :userId")
    int getBudgetCount(int userId);

    @Query("SELECT IFNULL(SUM(amount), 0.0) FROM budgets WHERE UserId = :userId")
    double getTotalBudget(int userId);

    @Query("SELECT IFNULL(SUM(b.amount), 0) " +
            "FROM budgets b JOIN categories c ON b.categoryId = c.id " +
            "WHERE b.userId = :userId AND c.name = :categoryName")
    double getTotalBudgetByCategoryName(int userId, String categoryName);
}
