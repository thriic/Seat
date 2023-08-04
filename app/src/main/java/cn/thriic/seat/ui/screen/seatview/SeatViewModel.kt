package cn.thriic.seat.ui.screen.seatview

import android.util.Log
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.thriic.seat.AppDataStore
import cn.thriic.seat.data.Seat
import cn.thriic.seat.data.SeatPos
import cn.thriic.seat.data.SeatType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class SeatViewModel @Inject constructor(private val appDataStore: AppDataStore) : ViewModel() {

    private val _uiState =
        MutableStateFlow(SeatUIState(null, "", false))
    val uiState: StateFlow<SeatUIState> = _uiState.asStateFlow()

    private val _seatState = with(appDataStore) {
        MutableStateFlow(SeatState(getAspectData(Pair(4, 8)), emptyList()))
    }
    val seatState: StateFlow<SeatState> = _seatState.asStateFlow()

    // val settings: Preferences by appDataStore.dataStore.data.collectAsState()

    private suspend fun updateUI(block: SeatUIState.() -> SeatUIState) {
        val newState: SeatUIState
        uiState.value.apply { newState = block() }
        _uiState.emit(newState)
    }

    private suspend fun update(block: SeatState.() -> SeatState) {
        val newState: SeatState
        seatState.value.apply { newState = block() }
        _seatState.emit(newState)
    }

    fun send(intent: SeatIntent) = viewModelScope.launch { onHandle(intent) }

    private suspend fun onHandle(intent: SeatIntent) {
        when (intent) {
            is SeatIntent.CanvasClick -> {
                val (x, y) = intent.offset
                for ((index, seat) in seatState.value.seats.withIndex()) {
                    if (seat.pos != null)
                        if (x in seat.pos.startX..seat.pos.endX && y in seat.pos.startY..seat.pos.endY) {
                            Log.i(TAG, "click canvas with index $index")
                            if (seat.person != null) Log.i(
                                TAG,
                                "Seat with Person(${Json.encodeToString(seat.person)})"
                            )
                            if (uiState.value.select?.equals(index) != true) {
                                updateUI {
                                    copy(select = index)
                                }
                            }
                        }
                }
            }

            is SeatIntent.Init -> {
                initSeatingChart(intent.size)
            }

            is SeatIntent.SearchClick -> {
                updateUI {
                    copy(select = intent.index)
                }
            }

            SeatIntent.UpdateSettings -> {
                if(appDataStore.getAspectData(seatState.value.size) != seatState.value.size) {
                    //initSeatingChart(seatState.value.size)
                    update {
                        copy(size = appDataStore.getAspectData(seatState.value.size))
                    }
                }
            }

            is SeatIntent.SetAvailable -> {
                val originSeat = seatState.value.seats[uiState.value.select!!]
                val seats: MutableList<Seat> = seatState.value.seats.toMutableList()
                if (intent.able) {
                    seats[uiState.value.select!!] =
                        originSeat.copy(type = SeatType.Unoccupied)
                } else {
                    seats[uiState.value.select!!] =
                        originSeat.copy(type = SeatType.Disabled, person = null)
                }
                update {
                    copy(seats = seats)
                }
                appDataStore.putSeatData(seats)
            }

            is SeatIntent.SetPerson -> {
                val originSeat = seatState.value.seats[uiState.value.select!!]
                val seats: MutableList<Seat> = seatState.value.seats.toMutableList()
                seats[uiState.value.select!!] =
                    originSeat.copy(
                        type = if (intent.person != null) SeatType.Occupied else SeatType.Unoccupied,
                        person = intent.person
                    )
                update {
                    copy(seats = seats)
                }
                appDataStore.putSeatData(seats)
            }
        }
    }


    private fun loadSeatingChart(): MutableList<Seat> {
        val plain =
            "[{\"type\":\"Occupied\",\"person\":{\"name\":\"陈思瑞\",\"id\":\"2022101063\"}}," +
                    "{\"type\":\"Unoccupied\"}," +
                    "{\"type\":\"Disabled\"},{\"type\":\"Occupied\",\"person\":{\"name\":\"文\",\"id\":\"20225646465\"}},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"},{\"type\":\"Unoccupied\"}]"
        return Json.decodeFromString(string = plain)
    }

    private suspend fun initSeatingChart(size: Size) {
        //if (seatState.value.seats.isNotEmpty()) return
        val (row, column) = seatState.value.size

        val ratio = 0.9f //占比
        val startX = size.width * (1 - ratio) / 2
        val startY = size.height * (1 - ratio) / 2
        val endX = size.width * (1 + ratio) / 2
        val endY = size.height * (1 + ratio) / 2
        val stepX = (endX - startX) / column
        val stepY = (endY - startY) / row
        val step = minOf(stepY,stepX)

        val seatsData = appDataStore.getSeatData()
        Log.i(TAG, "load datastore $seatsData")
        val seatsDataWithPos = mutableListOf<Seat>()
        for (i in 0 until row)
            for (j in 0 until column) {
                val index = (i * column + j % column) //由行列得到序号
                seatsDataWithPos.add(
                    seatsData[index].copy(
                        pos = SeatPos(
                            step * j + startX,
                            step * i + startY,
                            step * (j + 1) + startX,
                            step * (i + 1) + startY
                        )
                    )
                )
            }

        update {
            copy(seats = seatsDataWithPos)
        }
//        Log.i(TAG, Json.encodeToString(seatsDataWithPos))
    }

    companion object {
        const val TAG = "ViewModel"
    }
}