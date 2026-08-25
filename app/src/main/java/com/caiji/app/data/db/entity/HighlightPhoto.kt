package com.caiji.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 高光相册 - 单独存储大奖照片及对应的日记。
 * 与 [Diary] 关联（一对多：一篇日记可有多张照片）。
 */
@Entity(
    tableName = "highlight_photos",
    indices = [
        Index(value = ["diary_id"]),
        Index(value = ["taken_at"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Diary::class,
            parentColumns = ["id"],
            childColumns = ["diary_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class HighlightPhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 关联的日记，可空（独立照片） */
    @ColumnInfo(name = "diary_id")
    val diaryId: Long? = null,
    /** 照片本地路径（应用内部存储） */
    @ColumnInfo(name = "photo_path")
    val photoPath: String,
    /** 中奖金额 */
    @ColumnInfo(name = "prize_amount")
    val prizeAmount: Double,
    /** 描述 */
    val description: String = "",
    /** 拍照/记录时间 */
    @ColumnInfo(name = "taken_at")
    val takenAt: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
