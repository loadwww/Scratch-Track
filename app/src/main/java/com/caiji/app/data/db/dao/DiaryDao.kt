package com.caiji.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.caiji.app.data.db.entity.Diary
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diary: Diary): Long

    @Update
    suspend fun update(diary: Diary)

    @Delete
    suspend fun delete(diary: Diary)

    @Query("SELECT * FROM diaries ORDER BY record_time DESC")
    fun observeAll(): Flow<List<Diary>>

    @Query("SELECT * FROM diaries WHERE mood = :mood ORDER BY record_time DESC")
    fun observeByMood(mood: String): Flow<List<Diary>>

    @Query("SELECT * FROM diaries WHERE is_highlighted = 1 ORDER BY record_time DESC")
    fun observeHighlights(): Flow<List<Diary>>

    @Query("SELECT * FROM diaries WHERE id = :id")
    suspend fun getById(id: Long): Diary?

    @Query("UPDATE diaries SET is_highlighted = :highlighted, updated_at = :time WHERE id = :id")
    suspend fun setHighlighted(id: Long, highlighted: Boolean, time: Long = System.currentTimeMillis())

    @Query("UPDATE diaries SET is_highlighted = 0, updated_at = :time WHERE is_highlighted = 1 AND id = :id")
    suspend fun unhighlight(id: Long, time: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM diaries")
    suspend fun count(): Int
}
