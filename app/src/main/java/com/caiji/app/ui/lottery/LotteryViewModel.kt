package com.caiji.app.ui.lottery

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LotteryEditState(
    val id: Long? = null,
    val amount: String = "",
    val winAmount: String = "",
    val title: String = "",
    val note: String = "",
    val imagePath: String? = null,
    val isBigPrize: Boolean = false,
    val saved: Boolean = false
)

class LotteryViewModel(
    private val repo: LotteryRepository,
    private val diaryRepo: DiaryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LotteryEditState())
    val state: StateFlow<LotteryEditState> = _state.asStateFlow()

    fun load(id: Long?) {
        viewModelScope.launch {
            if (id != null) {
                repo.getById(id)?.let { r ->
                    _state.value = LotteryEditState(
                        id = r.id,
                        amount = if (r.amount == 0.0) "" else r.amount.toString(),
                        winAmount = if (r.winAmount == 0.0) "" else r.winAmount.toString(),
                        title = r.note.take(20),
                        note = r.note,
                        imagePath = r.imagePath,
                        isBigPrize = r.isBigPrize
                    )
                }
            } else {
                _state.value = LotteryEditState()
            }
        }
    }

    fun setAmount(v: String) {
        _state.value = _state.value.copy(
            amount = v.filter { c -> c.isDigit() || c == '.' }
        )
    }

    fun setTitle(v: String) {
        _state.value = _state.value.copy(title = v)
    }

    fun setWinAmount(v: String) {
        _state.value = _state.value.copy(
            winAmount = v.filter { c -> c.isDigit() || c == '.' }
        )
    }

    fun setNote(v: String) {
        _state.value = _state.value.copy(note = v)
    }

    fun setImagePath(path: String?) {
        _state.value = _state.value.copy(imagePath = path)
    }

    fun setBigPrize(b: Boolean) {
        _state.value = _state.value.copy(isBigPrize = b)
    }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            val amount = s.amount.toDoubleOrNull() ?: 0.0
            val win = s.winAmount.toDoubleOrNull() ?: 0.0

            val record = LotteryRecord(
                id = s.id ?: 0,
                amount = amount,
                winAmount = win,
                note = s.note,
                imagePath = s.imagePath,
                isBigPrize = s.isBigPrize || win >= 1000.0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val recordId = if (s.id == null) {
                repo.insert(record)
            } else {
                repo.update(record)
                s.id!!
            }

            // 有标题或文字描述或图片时自动创建日记
            if (s.title.isNotBlank() || s.note.isNotBlank() || s.imagePath != null) {
                val diary = Diary(
                    recordTime = System.currentTimeMillis(),
                    lotteryRecordId = recordId,
                    title = s.title.ifBlank { "投注记录" },
                    content = s.note,
                    mood = Diary.Mood.CALM,
                    coverImagePath = s.imagePath
                )
                diaryRepo.insert(diary)
            }

            _state.value = s.copy(saved = true)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repo.deleteById(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val ctx = CaiJiApplication.instance
                LotteryViewModel(
                    repo = ServiceLocator.provideLotteryRepo(ctx),
                    diaryRepo = ServiceLocator.provideDiaryRepo(ctx)
                )
            }
        }
    }
}
