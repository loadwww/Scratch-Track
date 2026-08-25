package com.caiji.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 彩票日记 - 用户对一次（或一段时间）投注的文字记录。
 * 可关联一条 [LotteryRecord]，支持情绪标签和配图。
 */
@Entity(
    tableName = "diaries",
    indices = [
        Index(value = ["record_time"]),
        Index(value = ["mood"]),
        Index(value = ["lottery_record_id"]),
        Index(value = ["is_highlighted"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = LotteryRecord::class,
            parentColumns = ["id"],
            childColumns = ["lottery_record_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Diary(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 自动记录的时间（即创建时间），用于日记排序 */
    @ColumnInfo(name = "record_time")
    val recordTime: Long,
    /** 关联的彩票投注记录，可空 */
    @ColumnInfo(name = "lottery_record_id")
    val lotteryRecordId: Long? = null,
    val title: String,
    val content: String,
    /** 情绪标签：兴奋/期待/平静/失落/懊恼 等 */
    val mood: String = Mood.CALM,
    /** 配图路径（应用内部存储） */
    @ColumnInfo(name = "cover_image_path")
    val coverImagePath: String? = null,
    /** 是否收藏（高光时刻） */
    @ColumnInfo(name = "is_highlighted")
    val isHighlighted: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** 情绪标签常量 */
    object Mood {
        const val EXCITED = "兴奋"
        const val HOPEFUL = "期待"
        const val CALM = "平静"
        const val LOST = "失落"
        const val REGRET = "懊恼"
        val ALL = listOf(EXCITED, HOPEFUL, CALM, LOST, REGRET)
    }
}
