package com.caiji.app.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caiji.app.CaiJiApplication
import com.caiji.app.data.ServiceLocator
import com.caiji.app.data.db.entity.Diary
import com.caiji.app.data.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiaryEditState(
    val id: Long? = null,
    val recordTime: Long = System.currentTimeMillis(),
    val title: String = "",
    val content: String = "",
    val coverImagePath: String? = null,
    val saved: Boolean = false
)

class DiaryEditorViewModel(
    private val repo: DiaryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DiaryEditState())
    val state: StateFlow<DiaryEditState> = _state.asStateFlow()

    fun load(id: Long?) {
        viewModelScope.launch {
            if (id != null) {
                repo.getById(id)?.let { d ->
                    _state.value = DiaryEditState(
                        id = d.id,
                        recordTime = d.recordTime,
                        title = d.title,
                        content = d.content,
                        coverImagePath = d.coverImagePath
                    )
                }
            } else {
                _state.value = DiaryEditState(recordTime = System.currentTimeMillis())
            }
        }
    }

    fun setTitle(v: String) { _state.value = _state.value.copy(title = v) }
    fun setContent(v: String) { _state.value = _state.value.copy(content = v) }
    fun setCover(path: String?) { _state.value = _state.value.copy(coverImagePath = path) }

    /** 只要标题/文字描述/附图有任一输入即可保存 */
    fun save() {
        viewModelScope.launch {
            val s = _state.value
            if (s.title.isBlank() && s.content.isBlank() && s.coverImagePath == null) return@launch
            val now = System.currentTimeMillis()
            val diary = Diary(
                id = s.id ?: 0,
                recordTime = s.recordTime,
                lotteryRecordId = null,
                title = s.title.ifBlank { "未命名" },
                content = s.content,
                mood = Diary.Mood.CALM,
                coverImagePath = s.coverImagePath,
                createdAt = now,
                updatedAt = now
            )
            if (s.id == null) repo.insert(diary) else repo.update(diary)
            _state.value = s.copy(saved = true)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val ctx = CaiJiApplication.instance
                DiaryEditorViewModel(repo = ServiceLocator.provideDiaryRepo(ctx))
            }
        }
    }
}
