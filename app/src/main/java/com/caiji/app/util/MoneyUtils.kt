package com.caiji.app.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object MoneyUtils {
    private val symbols = DecimalFormatSymbols(Locale.CHINA).apply { groupingSeparator = ',' }
    private val yuanFmt = DecimalFormat("¥#,##0.00", symbols)
    private val plainFmt = DecimalFormat("#,##0.00", symbols)
    private val intFmt = DecimalFormat("#,##0", symbols)

    /** 带人民币符号 + 两位小数，如 ¥1,234.50 */
    fun yuan(amount: Double): String = yuanFmt.format(amount)

    /** 不带符号，两位小数 */
    fun plain(amount: Double): String = plainFmt.format(amount)

    /** 整数（用于"元/月"这类汇总） */
    fun intAmount(amount: Double): String = intFmt.format(amount)

    /** 盈亏百分比字符串：+12.34% / -5.00% / 0.00% */
    fun pnlPercent(invest: Double, win: Double): String {
        if (invest <= 0.0) return "0.00%"
        val pct = (win - invest) / invest * 100.0
        val sign = if (pct > 0) "+" else ""
        return "$sign${plainFmt.format(pct)}%"
    }

    /** 盈亏金额：+100.00 / -50.00 */
    fun pnlAmount(invest: Double, win: Double): String {
        val diff = win - invest
        val sign = if (diff > 0) "+" else ""
        return "$sign${plainFmt.format(diff)}"
    }
}
