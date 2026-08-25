package com.caiji.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private val isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val isoMinute = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val isoSecond = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private val monthFmt = SimpleDateFormat("yyyy-MM", Locale.CHINA)

    init {
        isoDate.timeZone = TimeZone.getDefault()
        isoMinute.timeZone = TimeZone.getDefault()
        isoSecond.timeZone = TimeZone.getDefault()
        monthFmt.timeZone = TimeZone.getDefault()
    }

    fun formatDate(ts: Long): String = isoDate.format(Date(ts))
    fun formatDateTime(ts: Long): String = isoMinute.format(Date(ts))
    fun formatDateTimeSec(ts: Long): String = isoSecond.format(Date(ts))
    fun formatMonth(ts: Long): String = monthFmt.format(Date(ts))
    fun currentMonth(): String = monthFmt.format(Date())

    /** 返回某月（基于任意该月时间戳）的起止毫秒，本地时区。 */
    fun monthRange(anyTs: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeZone = TimeZone.getDefault()
            timeInMillis = anyTs
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun nextMonthStart(anyTs: Long): Long {
        val start = monthRange(anyTs)
        val cal = Calendar.getInstance().apply {
            timeZone = TimeZone.getDefault()
            timeInMillis = start
            add(Calendar.MONTH, 1)
        }
        return cal.timeInMillis
    }

    /** 当前月的 [start, end) 区间 */
    fun currentMonthRange(): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        return monthRange(now) to nextMonthStart(now)
    }
}
