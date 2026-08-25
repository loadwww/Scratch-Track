package com.caiji.app.data.repository

import com.caiji.app.data.db.CaiJiDatabase
import com.caiji.app.data.db.entity.Diary
import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val db: CaiJiDatabase) {

    private val dao get() = db.diaryDao()

    fun observeAll(): Flow<List<Diary>> = dao.observeAll()

    fun observeByMood(mood: String): Flow<List<Diary>> = dao.observeByMood(mood)

    fun observeHighlights(): Flow<List<Diary>> = dao.observeHighlights()

    suspend fun getById(id: Long): Diary? = dao.getById(id)

    suspend fun insert(diary: Diary): Long = dao.insert(diary)

    suspend fun update(diary: Diary) = dao.update(diary)

    suspend fun delete(diary: Diary) = dao.delete(diary)

    suspend fun setHighlighted(id: Long, highlighted: Boolean) =
        dao.setHighlighted(id, highlighted)
}
