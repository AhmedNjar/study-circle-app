package com.studycircle.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.studycircle.android.navigation.StudyCircleNavGraph
import com.studycircle.android.ui.theme.LocalThemeController
import com.studycircle.android.ui.theme.StudyCircleTheme
import com.studycircle.android.ui.theme.ThemeController
import com.studycircle.android.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeController = ThemeController(
                isDark = themeViewModel.isDarkTheme.value,
                onToggle = themeViewModel::toggleTheme
            )

            CompositionLocalProvider(LocalThemeController provides themeController) {
                StudyCircleTheme {
                    // RTL-first: the whole app is laid out right-to-left regardless of
                    // device locale (the content is Arabic by design).
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        StudyCircleNavGraph()
                    }
                }
            }
        }
    }
}
