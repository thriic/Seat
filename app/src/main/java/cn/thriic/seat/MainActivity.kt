package cn.thriic.seat

import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cn.thriic.seat.ui.screen.seatview.SeatView
import cn.thriic.seat.ui.screen.seatview.SeatViewModel
import cn.thriic.seat.ui.screen.setting.SettingView
import cn.thriic.seat.ui.screen.setting.SettingViewModel
import cn.thriic.seat.ui.theme.SeatTheme
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val seatViewModel: SeatViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            SeatTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "seat", modifier = Modifier.fillMaxSize()) {
                    composable("seat") {
                        SeatView(seatViewModel, navController)
                    }
                    composable("setting") {
                        SettingView(
                            settingViewModel,
                            navController
                        )
                    }
                }
            }
        }
    }
}