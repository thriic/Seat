package cn.thriic.seat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ShareActivity : ComponentActivity() {
    @Inject lateinit var appDataStore:AppDataStore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            try {

                val data = intent.readFromShare(this@ShareActivity)
                if (data != null) {
                    Toast.makeText(this@ShareActivity, "导入成功", Toast.LENGTH_SHORT).show()

                    appDataStore.putAspectData(data.aspect)
                    appDataStore.putSeatData(data.seats)
                }
                android.os.Process.killProcess(android.os.Process.myPid())
                this@ShareActivity.finish()


            } catch (e: Exception) {
                Toast.makeText(this@ShareActivity, "导入失败:${e.message}", Toast.LENGTH_SHORT).show()
                this@ShareActivity.finish()
            }
        }
    }
}