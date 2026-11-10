package com.livesport77.tv

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import com.livesport77.tv.ui.home.HomeScreen
import com.livesport77.tv.ui.navigation.TvKeyEventHandler
import com.livesport77.tv.ui.theme.LiveSport77TvTheme
import com.livesport77.tv.viewmodel.SportsViewModel

/**
 * Main Activity for Android TV.
 * Intercepts Hardware Remote KeyEvents and renders Compose for TV.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: SportsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveSport77TvTheme {
                HomeScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null) {
            // Check if user pressed Back key while in fullscreen
            if (keyCode == KeyEvent.KEYCODE_BACK && viewModel.uiState.value.isFullscreen) {
                viewModel.setFullscreen(false)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

