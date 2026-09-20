package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.screens.MainScaffold
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.PhoneCheckProTheme
import com.example.ui.viewmodel.DiagnosticsViewModel
import com.example.ui.viewmodel.StartupState
import com.example.util.SafeLog

class MainActivity : ComponentActivity() {

    private val viewModel: DiagnosticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (t: Throwable) {
            SafeLog.w("Failed to enableEdgeToEdge", t)
        }

        setContent {
            val isDark by viewModel.isDarkMode.collectAsState()
            val accent by viewModel.accentStyle.collectAsState()
            val startupState by viewModel.startupState.collectAsState()

            PhoneCheckProTheme(darkTheme = isDark, accent = accent) {
                Crossfade(
                    targetState = startupState,
                    animationSpec = tween(durationMillis = 400),
                    label = "startup_crossfade"
                ) { state ->
                    when (state) {
                        is StartupState.Loading -> {
                            SplashScreen(
                                progress = state.progress,
                                statusText = state.statusText
                            )
                        }
                        is StartupState.Ready -> {
                            MainScaffold(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
