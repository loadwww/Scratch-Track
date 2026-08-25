package com.caiji.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 彩票投注记录 - 核心数据。
 * 简化版：仅包含投入金额、中奖金额，可选备注和图片。
 */
@Entity(
    tableName = "lottery_records",
    indices = [
        Index(value = ["created_at"])
    ]
)
data class LotteryRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 投入金额 */
    val amount: Double,
    /** 中奖金额，未开奖或未中为 0 */
    @ColumnInfo(name = "win_amount")
    val winAmount: Double = 0.0,
    /** 备注/描述（可选） */
    val note: String = "",
    /** 图片路径（可选） */
    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,
    /** 是否大奖（默认 ≥ 1000 视为大奖，可在设置中调整） */
    @ColumnInfo(name = "is_big_prize")
    val isBigPrize: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
