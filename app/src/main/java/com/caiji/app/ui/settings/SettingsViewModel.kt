package com.caiji.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.caiji.app.CaiJiApplication
import com.caiji.app.data.ServiceLocator
import com.caiji.app.data.prefs.BackgroundTheme
import com.caiji.app.data.prefs.CaiJiSettings
import com.caiji.app.data.prefs.SettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val store: SettingsStore
) : ViewModel() {

    val settings: StateFlow<CaiJiSettings> =
        store.settings.stateIn(viewModelScope, SharingStarted.Eagerly, CaiJiSettings())

    fun setMarqueeTexts(texts: List<String>) = viewModelScope.launch { store.updateMarqueeTexts(texts) }
    fun setBackground(theme: BackgroundTheme) = viewModelScope.launch { store.updateBackground(theme) }
    fun setMonthlyBudgetDefault(amount: Double) = viewModelScope.launch { store.updateMonthlyBudget(amount) }
    fun setWallpaper(path: String) = viewModelScope.launch { store.updateWallpaper(path) }
    fun clearWallpaper() = viewModelScope.launch { store.updateWallpaper("") }
    fun setMusicEnabled(enabled: Boolean) = viewModelScope.launch { store.updateMusicEnabled(enabled) }
    fun setMusicPath(path: String) = viewModelScope.launch { store.updateMusicPath(path) }
    fun clearMusicPath() = viewModelScope.launch { store.updateMusicPath("") }
    fun setBudgetNearAlert(text: String) = viewModelScope.launch { store.updateBudgetNearAlert(text) }
    fun setBudgetExceedAlert(text: String) = viewModelScope.launch { store.updateBudgetExceedAlert(text) }
    fun setScratchDailyCoins(coins: Int) = viewModelScope.launch { store.updateScratchDailyCoins(coins) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val ctx = CaiJiApplication.instance
                SettingsViewModel(store = ServiceLocator.provideSettings(ctx))
            }
        }
    }
}
