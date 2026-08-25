package com.caiji.app.ui.highlight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caiji.app.CaiJiApplication
import com.caiji.app.data.ServiceLocator
import com.caiji.app.data.db.entity.Diary
import com.caiji.app.data.repository.DiaryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HighlightViewModel(
    private val diaryRepo: DiaryRepository
) : ViewModel() {

    /** 高光时刻 = 已收藏的彩票日记 */
    val highlightedDiaries: StateFlow<List<Diary>> =
        diaryRepo.observeHighlights().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** 取消收藏（只修改高光标记，不删除日记本身） */
    fun unhighlight(diary: Diary) {
        viewModelScope.launch {
            diaryRepo.setHighlighted(diary.id, false)
        }
    }

    /** 直接删除日记（同时删除日记本身） */
    fun deleteDiary(diary: Diary) {
        viewModelScope.launch {
            diaryRepo.delete(diary)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val ctx = CaiJiApplication.instance
                HighlightViewModel(diaryRepo = ServiceLocator.provideDiaryRepo(ctx))
            }
        }
    }
}
