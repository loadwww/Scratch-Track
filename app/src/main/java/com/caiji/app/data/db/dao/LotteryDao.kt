package com.caiji.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.caiji.app.data.db.entity.LotteryRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface LotteryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: LotteryRecord): Long

    @Update
    suspend fun update(record: LotteryRecord)

    @Delete
    suspend fun delete(record: LotteryRecord)

    @Query("SELECT * FROM lottery_records ORDER BY created_at DESC")
    fun observeAll(): Flow<List<LotteryRecord>>

    @Query("SELECT * FROM lottery_records WHERE id = :id")
    suspend fun getById(id: Long): LotteryRecord?

    /** 全局总投入 */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM lottery_records")
    fun observeTotalInvest(): Flow<Double>

    /** 全局总中奖 */
    @Query("SELECT COALESCE(SUM(win_amount), 0.0) FROM lottery_records")
    fun observeTotalWin(): Flow<Double>

    /** 某月（毫秒区间）的总投入 */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM lottery_records WHERE created_at >= :start AND created_at < :end")
    fun observeInvestBetween(start: Long, end: Long): Flow<Double>

    /** 某月的总中奖 */
    @Query("SELECT COALESCE(SUM(win_amount), 0.0) FROM lottery_records WHERE created_at >= :start AND created_at < :end")
    fun observeWinBetween(start: Long, end: Long): Flow<Double>

    /** 某月的净盈亏 */
    @Query("SELECT COALESCE(SUM(win_amount - amount), 0.0) FROM lottery_records WHERE created_at >= :start AND created_at < :end")
    fun observePnlBetween(start: Long, end: Long): Flow<Double>

    /** 月度盈亏：返回月份字符串、投入合计、中奖合计 */
    @Query("""
        SELECT strftime('%Y-%m', created_at / 1000, 'unixepoch', 'localtime') AS month,
               COALESCE(SUM(amount), 0.0) AS invest,
               COALESCE(SUM(win_amount), 0.0) AS win
        FROM lottery_records
        GROUP BY month
        ORDER BY month DESC
        LIMIT :limitMonths
    """)
    fun observeMonthlySummary(limitMonths: Int = 12): Flow<List<MonthlySummaryRow>>

    /** 所有历史月度汇总（无limit） */
    @Query("""
        SELECT strftime('%Y-%m', created_at / 1000, 'unixepoch', 'localtime') AS month,
               COALESCE(SUM(amount), 0.0) AS invest,
               COALESCE(SUM(win_amount), 0.0) AS win
        FROM lottery_records
        GROUP BY month
        ORDER BY month DESC
    """)
    fun observeAllMonthlySummary(): Flow<List<MonthlySummaryRow>>

    /** 日度盈亏（用于月内图表） */
    @Query("""
        SELECT strftime('%Y-%m-%d', created_at / 1000, 'unixepoch', 'localtime') AS day,
               COALESCE(SUM(amount), 0.0) AS invest,
               COALESCE(SUM(win_amount), 0.0) AS win
        FROM lottery_records
        WHERE created_at >= :start AND created_at < :end
        GROUP BY day
        ORDER BY day ASC
    """)
    fun observeDailySummaryBetween(start: Long, end: Long): Flow<List<DailySummaryRow>>

    /** 按ID删除 */
    @Query("DELETE FROM lottery_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}

data class MonthlySummaryRow(
    val month: String,
    val invest: Double,
    val win: Double
)

data class DailySummaryRow(
    val day: String,
    val invest: Double,
    val win: Double
)
