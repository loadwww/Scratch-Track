package com.caiji.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.caiji.app.data.db.dao.BudgetDao
import com.caiji.app.data.db.dao.DiaryDao
import com.caiji.app.data.db.dao.HighlightDao
import com.caiji.app.data.db.dao.LotteryDao
import com.caiji.app.data.db.entity.Diary
import com.caiji.app.data.db.entity.HighlightPhoto
import com.caiji.app.data.db.entity.LotteryRecord
import com.caiji.app.data.db.entity.MonthlyBudget

@Database(
    entities = [
        LotteryRecord::class,
        Diary::class,
        HighlightPhoto::class,
        MonthlyBudget::class
    ],
    version = 3,
    exportSchema = false
)
abstract class CaiJiDatabase : RoomDatabase() {
    abstract fun lotteryDao(): LotteryDao
    abstract fun diaryDao(): DiaryDao
    abstract fun highlightDao(): HighlightDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: CaiJiDatabase? = null

        fun get(context: Context): CaiJiDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CaiJiDatabase::class.java,
                    "caiji.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
