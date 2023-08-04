package cn.thriic.seat.ui.screen.seatview

import cn.thriic.seat.data.Seat

data class SeatUIState(
    val select: Int?,
    val searchText: String,
    val showRestart: Boolean
)

data class SeatState(
    val size: Pair<Int, Int>,
    val seats: List<Seat>
)