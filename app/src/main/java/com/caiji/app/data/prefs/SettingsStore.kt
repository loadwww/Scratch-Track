package com.caiji.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "caiji_prefs")

/** 用户可配置项（持久化） */
data class CaiJiSettings(
    /** 首页轮换文字，用 "|" 分隔多条句子 */
    val marqueeTexts: List<String> = listOf("见好就收"),
    val background: BackgroundTheme = BackgroundTheme.CRIMSON,
    val monthlyBudgetDefault: Double = 500.0,
    /** 首页壁纸背景图本地路径（空字符串表示无壁纸） */
    val wallpaperPath: String = "",
    /** 盈利时是否播放音乐 */
    val musicEnabled: Boolean = true,
    /** 自定义音乐文件路径（空字符串表示使用内置《好运来》） */
    val musicPath: String = "",
    /** 预算接近上限（≥80%）时的提醒文字 */
    val budgetNearAlert: String = "预算已使用超过80%，请注意控制！",
    /** 预算超支（≥100%）时的提醒文字 */
    val budgetExceedAlert: String = "本月预算已超支，请谨慎投注！",
    /** 刮刮乐每日领取金币数 */
    val scratchDailyCoins: Int = 100
)

/** 刮刮乐虚拟金币数据 */
data class ScratchData(
    val coins: Int = 0,
    val totalWinnings: Int = 0,
    val lastClaimDate: String = ""
)

enum class BackgroundTheme(val title: String, val value: String) {
    DEFAULT("默认浅紫", "default"),
    WARM("暖色玫瑰", "warm"),
    COOL("冷色海洋", "cool"),
    DARK("深邃黑", "dark"),
    CRIMSON("亮红", "crimson");

    companion object {
        fun fromValue(v: String?) = entries.firstOrNull { it.value == v } ?: CRIMSON
    }
}

class SettingsStore(private val context: Context) {

    private object Keys {
        val MARQUEE = stringPreferencesKey("marquee_text")
        val BACKGROUND = stringPreferencesKey("background_theme")
        val BUDGET = doublePreferencesKey("monthly_budget_default")
        val WALLPAPER = stringPreferencesKey("wallpaper_path")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val MUSIC_PATH = stringPreferencesKey("music_path")
        val BUDGET_NEAR_ALERT = stringPreferencesKey("budget_near_alert")
        val BUDGET_EXCEED_ALERT = stringPreferencesKey("budget_exceed_alert")
        val SCRATCH_DAILY_COINS = intPreferencesKey("scratch_daily_coins")
        val SCRATCH_COINS = intPreferencesKey("scratch_coins")
        val SCRATCH_WINNINGS = intPreferencesKey("scratch_winnings")
        val SCRATCH_LAST_CLAIM = stringPreferencesKey("scratch_last_claim")
    }

    val settings: Flow<CaiJiSettings> = context.dataStore.data.map { it.toSettings() }

    suspend fun updateMarqueeTexts(texts: List<String>) =
        context.dataStore.edit { it[Keys.MARQUEE] = texts.joinToString("|") }
    suspend fun updateBackground(theme: BackgroundTheme) =
        context.dataStore.edit { it[Keys.BACKGROUND] = theme.value }
    suspend fun updateMonthlyBudget(amount: Double) =
        context.dataStore.edit { it[Keys.BUDGET] = amount }
    suspend fun updateWallpaper(path: String) =
        context.dataStore.edit { it[Keys.WALLPAPER] = path }
    suspend fun updateMusicEnabled(enabled: Boolean) =
        context.dataStore.edit { it[Keys.MUSIC_ENABLED] = enabled }
    suspend fun updateMusicPath(path: String) =
        context.dataStore.edit { it[Keys.MUSIC_PATH] = path }
    suspend fun updateBudgetNearAlert(text: String) =
        context.dataStore.edit { it[Keys.BUDGET_NEAR_ALERT] = text }
    suspend fun updateBudgetExceedAlert(text: String) =
        context.dataStore.edit { it[Keys.BUDGET_EXCEED_ALERT] = text }
    suspend fun updateScratchDailyCoins(coins: Int) =
        context.dataStore.edit { it[Keys.SCRATCH_DAILY_COINS] = coins }

    // 刮刮乐
    val scratchData: Flow<ScratchData> = context.dataStore.data.map {
        ScratchData(
            coins = it[Keys.SCRATCH_COINS] ?: 0,
            totalWinnings = it[Keys.SCRATCH_WINNINGS] ?: 0,
            lastClaimDate = it[Keys.SCRATCH_LAST_CLAIM] ?: ""
        )
    }

    suspend fun addCoins(amount: Int, claimDate: String) =
        context.dataStore.edit { prefs ->
            prefs[Keys.SCRATCH_COINS] = (prefs[Keys.SCRATCH_COINS] ?: 0) + amount
            prefs[Keys.SCRATCH_LAST_CLAIM] = claimDate
        }

    suspend fun scratchResult(faceValue: Int, prize: Int) =
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.SCRATCH_COINS] ?: 0
            prefs[Keys.SCRATCH_COINS] = current - faceValue + prize
            prefs[Keys.SCRATCH_WINNINGS] = (prefs[Keys.SCRATCH_WINNINGS] ?: 0) + prize
        }

    private fun Preferences.toSettings(): CaiJiSettings = CaiJiSettings(
        marqueeTexts = (this[Keys.MARQUEE] ?: "见好就收")
            .split("|")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf("见好就收") },
        background = BackgroundTheme.fromValue(this[Keys.BACKGROUND]),
        monthlyBudgetDefault = this[Keys.BUDGET] ?: 500.0,
        wallpaperPath = this[Keys.WALLPAPER] ?: "",
        musicEnabled = this[Keys.MUSIC_ENABLED] ?: true,
        musicPath = this[Keys.MUSIC_PATH] ?: "",
        budgetNearAlert = this[Keys.BUDGET_NEAR_ALERT] ?: "预算已使用超过80%，请注意控制！",
        budgetExceedAlert = this[Keys.BUDGET_EXCEED_ALERT] ?: "本月预算已超支，请谨慎投注！",
        scratchDailyCoins = this[Keys.SCRATCH_DAILY_COINS] ?: 100
    )
}
