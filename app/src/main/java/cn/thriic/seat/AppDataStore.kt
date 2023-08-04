package cn.thriic.seat

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import cn.thriic.seat.data.Seat
import cn.thriic.seat.data.SeatType
import cn.thriic.seat.model.Constant.KEY_ASPECT
import cn.thriic.seat.model.Constant.KEY_SEAT
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Module
@InstallIn(ActivityComponent::class)
class AppDataStore @Inject constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun putSeatData(value: List<Seat>) {
        putString(KEY_SEAT, Json.encodeToString(value))
    }

    suspend fun putAspectData(value: Pair<Int, Int>) {
        putString(KEY_ASPECT, Json.encodeToString(value))
    }

    fun getSeatData(): List<Seat> = runBlocking {
        val string = getString(KEY_SEAT)
        val size = with(getAspectData(Pair(3, 6))) { first * second }
        return@runBlocking if (string == null) {
            //生成空白座位表
            val list = mutableListOf<Seat>()
            repeat(size) {
                list.add(Seat(SeatType.Unoccupied))
            }
            putSeatData(list)
            list
        } else {
            val list: MutableList<Seat> = Json.decodeFromString(string = string)
            when {
                list.size == size -> list
                //储存座位表数量小于规定数量，添加空白座位
                list.size < size -> {
                    repeat(size - list.size) {
                        list.add(Seat(SeatType.Unoccupied))
                    }
                    putSeatData(list)
                    list
                }
                //储存座位表数量大于规定数量，删除多余座位
                else -> {
                    list.subList(0,size).also { putSeatData(it) }
                }
            }
        }
    }

    fun getAspectData(default: Pair<Int, Int>): Pair<Int, Int> = runBlocking {
        val string = getString(KEY_ASPECT)
        return@runBlocking if (string == null) {
            putAspectData(default)
            default
        } else {
            Json.decodeFromString(string = string)
        }
    }

    private suspend fun putString(key: Preferences.Key<String>, value: String) = dataStore.edit {
        it[key] = value
    }

    private fun getString(key: Preferences.Key<String>): String? = runBlocking {
        return@runBlocking dataStore.data.map {
            it[key]
        }.first()
    }

}