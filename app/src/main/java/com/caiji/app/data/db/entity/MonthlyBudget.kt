package com.caiji.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 月度购彩预算 - 每月一条。
 * 用于首页"接近预算上限时显示浅色提醒"。
 */
@Entity(
    tableName = "monthly_budgets",
    indices = [Index(value = ["month"], unique = true)]
)
data class MonthlyBudget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 月份，格式 yyyy-MM */
    val month: String,
    /** 预算金额（元） */
    @ColumnInfo(name = "budget_amount")
    val budgetAmount: Double,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
