package cn.thriic.seat.ui.screen.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.thriic.seat.AppDataStore
import cn.thriic.seat.ui.screen.seatview.SeatIntent
import cn.thriic.seat.ui.screen.seatview.SeatUIState
import cn.thriic.seat.ui.screen.seatview.SeatViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(private val appDataStore: AppDataStore) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            SettingState(
                appDataStore.getAspectData(Pair(4, 8)),
                appDataStore.getSeatData()
            )
        )
    val uiState: StateFlow<SettingState> = _uiState.asStateFlow()

    private suspend fun update(block: SettingState.() -> SettingState) {
        val newState: SettingState
        uiState.value.apply { newState = block() }
        _uiState.emit(newState)
    }

    fun send(intent: SettingIntent) = viewModelScope.launch { onHandle(intent) }

    private suspend fun onHandle(intent: SettingIntent) {
        when (intent) {
            SettingIntent.Export -> TODO()
            is SettingIntent.Import -> TODO()
            is SettingIntent.UpdateAspect -> {
                update {
                    copy(aspect = intent.aspect)
                }
                appDataStore.putAspectData(intent.aspect)
            }
        }
    }

}