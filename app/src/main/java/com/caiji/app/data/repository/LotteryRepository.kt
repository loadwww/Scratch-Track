package com.caiji.app.data.repository

import com.caiji.app.data.db.CaiJiDatabase
import com.caiji.app.data.db.dao.DailySummaryRow
import com.caiji.app.data.db.dao.MonthlySummaryRow
import com.caiji.app.data.db.entity.LotteryRecord
import com.caiji.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.roundToLong

class LotteryRepository(private val db: CaiJiDatabase) {

    private val dao get() = db.lotteryDao()

    fun observeAll(): Flow<List<LotteryRecord>> = dao.observeAll()
    fun observeTotalInvest(): Flow<Double> = dao.observeTotalInvest()
    fun observeTotalWin(): Flow<Double> = dao.observeTotalWin()

    fun observeMonthlySummary(limit: Int = 12): Flow<List<MonthlySummaryRow>> =
        dao.observeMonthlySummary(limit)

    fun observeAllMonthlySummary(): Flow<List<MonthlySummaryRow>> =
        dao.observeAllMonthlySummary()

    fun observeDailySummaryThisMonth(): Flow<List<DailySummaryRow>> {
        val (start, end) = DateUtils.currentMonthRange()
        return dao.observeDailySummaryBetween(start, end)
    }

    /** 当月投入 */
    fun observeInvestThisMonth(): Flow<Double> {
        val (start, end) = DateUtils.currentMonthRange()
        return dao.observeInvestBetween(start, end)
    }

    /** 当月中奖 */
    fun observeWinThisMonth(): Flow<Double> {
        val (start, end) = DateUtils.currentMonthRange()
        return dao.observeWinBetween(start, end)
    }

    /** 当月净盈亏 */
    fun observePnlThisMonth(): Flow<Double> {
        val (start, end) = DateUtils.currentMonthRange()
        return dao.observePnlBetween(start, end)
    }

    suspend fun getById(id: Long): LotteryRecord? = dao.getById(id)

    suspend fun insert(record: LotteryRecord): Long = dao.insert(record)

    suspend fun update(record: LotteryRecord) = dao.update(record)

    suspend fun delete(record: LotteryRecord) = dao.delete(record)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    /**
     * 首次启动时若投注数据为空，插入近 12 个月的演示数据，用于展示历史盈亏图表。
     */
    suspend fun seedDemoHistoryIfEmpty() {
        val existing = dao.observeTotalInvest().firstOrNull() ?: 0.0
        if (existing > 0.0) return

        val rnd = ThreadLocalRandom.current()
        val today = Calendar.getInstance()
        val records = mutableListOf<LotteryRecord>()
        // 生成近 12 个月：上月、前2月…前11月 + 本月也给少量
        for (monthOffset in 0 until 12) {
            val monthCal = Calendar.getInstance().apply {
                time = today.time
                add(Calendar.MONTH, -monthOffset)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val maxDay = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            // 每月 3-6 条记录
            val recordCount = rnd.nextInt(3, 7)
            repeat(recordCount) {
                val day = rnd.nextInt(1, maxDay + 1)
                monthCal.set(Calendar.DAY_OF_MONTH, day)
                monthCal.set(Calendar.HOUR_OF_DAY, rnd.nextInt(9, 22))
                monthCal.set(Calendar.MINUTE, rnd.nextInt(0, 60))
                val createdAt = monthCal.timeInMillis
                // 每次投入 10 / 20 / 30 / 50 / 100
                val base = listOf(10.0, 20.0, 30.0, 50.0, 100.0)
                val invest = base[rnd.nextInt(base.size)] * rnd.nextInt(1, 4)
                // 中奖：~20% 概率中，金额为投入的 0.2~3.5 倍
                val win = if (rnd.nextDouble() < 0.22) {
                    val factor = 0.2 + rnd.nextDouble() * 3.4
                    (invest * factor * 100.0).roundToLong() / 100.0
                } else 0.0
                records += LotteryRecord(
                    amount = invest,
                    winAmount = win,
                    note = "",
                    createdAt = createdAt,
                    updatedAt = createdAt,
                    isBigPrize = win >= 1000.0
                )
            }
        }
        records.forEach { dao.insert(it) }
    }
}
