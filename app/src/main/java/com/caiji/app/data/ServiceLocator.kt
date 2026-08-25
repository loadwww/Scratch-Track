package com.caiji.app.data

import android.content.Context
import com.caiji.app.data.db.CaiJiDatabase
import com.caiji.app.data.prefs.SettingsStore
import com.caiji.app.data.repository.BudgetRepository
import com.caiji.app.data.repository.DiaryRepository
import com.caiji.app.data.repository.HighlightRepository
import com.caiji.app.data.repository.LotteryRepository

/**
 * 手动 DI - 集中持有数据库、设置、各仓库的单例。
 */
object ServiceLocator {

    @Volatile
    private var database: CaiJiDatabase? = null
    private var settingsStore: SettingsStore? = null
    private var lotteryRepo: LotteryRepository? = null
    private var diaryRepo: DiaryRepository? = null
    private var highlightRepo: HighlightRepository? = null
    private var budgetRepo: BudgetRepository? = null

    fun provideDatabase(context: Context): CaiJiDatabase =
        database ?: synchronized(this) {
            database ?: CaiJiDatabase.get(context).also { database = it }
        }

    fun provideSettings(context: Context): SettingsStore =
        settingsStore ?: synchronized(this) {
            settingsStore ?: SettingsStore(context.applicationContext).also { settingsStore = it }
        }

    fun provideLotteryRepo(context: Context): LotteryRepository =
        lotteryRepo ?: synchronized(this) {
            lotteryRepo ?: LotteryRepository(provideDatabase(context)).also { lotteryRepo = it }
        }

    fun provideDiaryRepo(context: Context): DiaryRepository =
        diaryRepo ?: synchronized(this) {
            diaryRepo ?: DiaryRepository(provideDatabase(context)).also { diaryRepo = it }
        }

    fun provideHighlightRepo(context: Context): HighlightRepository =
        highlightRepo ?: synchronized(this) {
            highlightRepo ?: HighlightRepository(provideDatabase(context)).also { highlightRepo = it }
        }

    fun provideBudgetRepo(context: Context): BudgetRepository =
        budgetRepo ?: synchronized(this) {
            budgetRepo ?: BudgetRepository(provideDatabase(context)).also { budgetRepo = it }
        }
}
