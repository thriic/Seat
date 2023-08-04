package cn.thriic.seat.model

import androidx.datastore.preferences.core.stringPreferencesKey

object Constant {
    const val DATA_STORE_NAME = "seat"
    val KEY_ASPECT = stringPreferencesKey("aspect")
    val KEY_SEAT = stringPreferencesKey("seat")
}