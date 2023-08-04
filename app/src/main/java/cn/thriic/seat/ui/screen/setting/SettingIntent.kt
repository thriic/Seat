package cn.thriic.seat.ui.screen.setting

sealed class SettingIntent {
    data class UpdateAspect(val aspect: Pair<Int, Int>) : SettingIntent()
    data class Import(val seats: String) : SettingIntent()
    object Export : SettingIntent()
}