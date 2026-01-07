package com.example.chargingvinfast

import android.app.Application
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

    private val _uiState = MutableStateFlow(ChargingUiState())
    val uiState: StateFlow<ChargingUiState> = _uiState.asStateFlow()

    fun startCharging() {
        scheduler.startCharging()
        _uiState.update { it.copy(isRunning = true, startedAtMillis = System.currentTimeMillis()) }
    }

    fun stopCharging() {
        scheduler.stopCharging()
        _uiState.update { ChargingUiState(isRunning = false, startedAtMillis = null) }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val scheduler = ChargingScheduler(application.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(scheduler, application) as T
        }
    }
}
