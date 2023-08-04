package cn.thriic.seat.ui.screen.setting

import cn.thriic.seat.data.Seat

data class SettingState(val aspect: Pair<Int, Int>,val seats: List<Seat>)