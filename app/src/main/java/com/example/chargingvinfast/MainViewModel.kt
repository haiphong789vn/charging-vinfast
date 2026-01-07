package com.example.chargingvinfast

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ChargingUiState(
    val isRunning: Boolean = false,
    val startedAtMillis: Long? = null,
)

class MainViewModel(private val scheduler: ChargingScheduler, application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow(loadState())
    val uiState: StateFlow<ChargingUiState> = _uiState.asStateFlow()

    init {
        // Restore state from preferences
        val state = loadState()
        if (state.isRunning) {
            // Re-schedule if was running
            scheduler.startCharging()
        }
    }

    private fun loadState(): ChargingUiState {
        val isRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
        val startedAt = prefs.getLong(KEY_STARTED_AT, -1L).takeIf { it != -1L }
        return ChargingUiState(isRunning = isRunning, startedAtMillis = startedAt)
    }

    private fun saveState(state: ChargingUiState) {
        prefs.edit()
            .putBoolean(KEY_IS_RUNNING, state.isRunning)
            .putLong(KEY_STARTED_AT, state.startedAtMillis ?: -1L)
            .apply()
    }

    fun startCharging() {
        scheduler.startCharging()
        val newState = ChargingUiState(isRunning = true, startedAtMillis = System.currentTimeMillis())
        _uiState.update { newState }
        saveState(newState)
    }

    fun stopCharging() {
        scheduler.stopCharging()
        val newState = ChargingUiState(isRunning = false, startedAtMillis = null)
        _uiState.update { newState }
        saveState(newState)
    }

    companion object {
        private const val PREFS_NAME = "charging_prefs"
        private const val KEY_IS_RUNNING = "is_running"
        private const val KEY_STARTED_AT = "started_at"
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val scheduler = ChargingScheduler(application.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(scheduler, application) as T
        }
    }
}
