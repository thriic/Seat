package cn.thriic.seat.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Seat(val type: SeatType, val person: Person? = null, @Transient val pos: SeatPos? = null)

data class SeatPos(val startX: Float, val startY: Float, val endX: Float, val endY: Float)

@Serializable
data class Person(val name: String, val id: String = "")

enum class SeatType { Unoccupied, Occupied, Disabled }