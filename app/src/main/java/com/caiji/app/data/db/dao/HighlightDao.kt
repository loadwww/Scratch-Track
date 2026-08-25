package com.caiji.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.caiji.app.data.db.entity.HighlightPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: HighlightPhoto): Long

    @Update
    suspend fun update(photo: HighlightPhoto)

    @Delete
    suspend fun delete(photo: HighlightPhoto)

    @Query("SELECT * FROM highlight_photos ORDER BY taken_at DESC")
    fun observeAll(): Flow<List<HighlightPhoto>>

    @Query("SELECT * FROM highlight_photos WHERE diary_id = :diaryId ORDER BY taken_at DESC")
    fun observeByDiary(diaryId: Long): Flow<List<HighlightPhoto>>
}
