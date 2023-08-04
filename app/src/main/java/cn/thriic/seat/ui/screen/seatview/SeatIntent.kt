package cn.thriic.seat.ui.screen.seatview

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import cn.thriic.seat.data.Person

sealed class SeatIntent {
    data class Init(val size: Size) : SeatIntent()
    data class CanvasClick(val offset: Offset) : SeatIntent()
    data class SearchClick(val index: Int) : SeatIntent()
    data class SetPerson(val person: Person?) : SeatIntent()
    data class SetAvailable(val able: Boolean): SeatIntent()
    object UpdateSettings : SeatIntent()
}