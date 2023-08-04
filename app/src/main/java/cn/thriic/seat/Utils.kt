package cn.thriic.seat

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import cn.thriic.seat.data.Seat
import cn.thriic.seat.ui.screen.setting.SettingState
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File

fun Context.share(content: String) {
    val file = File(externalCacheDir, "座位表.seat")
    file.writeText(content)
    if (file.exists()) {
        val share = Intent(Intent.ACTION_SEND);
        val contentUri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".fileprovider",
            file
        )
        share.putExtra(Intent.EXTRA_STREAM, contentUri)
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        share.type = "text/plain"
        share.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        startActivity(Intent.createChooser(share, "分享文件"))
    } else {
        Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show()
    }
}

fun Intent.readFromShare(context: Context): SettingState? {
    Log.i("share", "action:$action type:$type")

    if (action != Intent.ACTION_SEND && action != Intent.ACTION_VIEW) return null

    val uri = this.data//parcelable<Uri>(Intent.EXTRA_STREAM)
    Log.i("share", "uri:$uri")

    if (uri != null) {
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val plain = BufferedReader(inputStream.reader()).readText()
            inputStream.close()
            println(plain)
            val text = plain.split('\n')
            try {
                val aspect = Json.decodeFromString<Pair<Int, Int>>(text[0])
                val seats = Json.decodeFromString<List<Seat>>(text[1])
                Log.i("ss", "获取成功")
                return SettingState(aspect, seats)
            } catch (_: Exception) {
                throw Exception("文件格式错误")
            }
        }
    }
    return null
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra("data")
}

fun Int.toMatrix(column: Int) = Pair(this / column + 1, this % column + 1)