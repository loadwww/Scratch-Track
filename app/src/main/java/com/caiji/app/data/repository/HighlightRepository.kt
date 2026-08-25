package com.caiji.app.data.repository

import com.caiji.app.data.db.CaiJiDatabase
import com.caiji.app.data.db.entity.HighlightPhoto
import kotlinx.coroutines.flow.Flow

class HighlightRepository(private val db: CaiJiDatabase) {

    private val dao get() = db.highlightDao()

    fun observeAll(): Flow<List<HighlightPhoto>> = dao.observeAll()

    fun observeByDiary(diaryId: Long): Flow<List<HighlightPhoto>> = dao.observeByDiary(diaryId)

    suspend fun insert(photo: HighlightPhoto): Long = dao.insert(photo)

    suspend fun update(photo: HighlightPhoto) = dao.update(photo)

    suspend fun delete(photo: HighlightPhoto) = dao.delete(photo)
}
