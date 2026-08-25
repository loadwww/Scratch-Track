package com.caiji.app.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caiji.app.CaiJiApplication
import com.caiji.app.data.ServiceLocator
import com.caiji.app.data.db.entity.Diary
import com.caiji.app.data.db.entity.LotteryRecord
import com.caiji.app.data.repository.DiaryRepository
import com.caiji.app.data.repository.LotteryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiaryViewModel(
    private val diaryRepo: DiaryRepository,
    private val lotteryRepo: LotteryRepository
) : ViewModel() {

    /** 中奖记录 - 独立列表 */
    val lotteryRecords: StateFlow<List<LotteryRecord>> = lotteryRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** 彩票日记 - 独立列表，按时间排序 */
    val diaries: StateFlow<List<Diary>> = diaryRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** 删除彩票日记 - 不影响中奖记录 */
    fun deleteDiary(diary: Diary) {
        viewModelScope.launch {
            diaryRepo.delete(diary)
        }
    }

    /** 删除中奖记录 - 同步更新统计（Flow 自动触发） */
    fun deleteLotteryRecord(record: LotteryRecord) {
        viewModelScope.launch {
            lotteryRepo.delete(record)
        }
    }

    /** 切换日记的收藏/高光状态 */
    fun toggleHighlight(diary: Diary) {
        viewModelScope.launch {
            diaryRepo.setHighlighted(diary.id, !diary.isHighlighted)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val ctx = CaiJiApplication.instance
                DiaryViewModel(
                    diaryRepo = ServiceLocator.provideDiaryRepo(ctx),
                    lotteryRepo = ServiceLocator.provideLotteryRepo(ctx)
                )
            }
        }
    }
}
