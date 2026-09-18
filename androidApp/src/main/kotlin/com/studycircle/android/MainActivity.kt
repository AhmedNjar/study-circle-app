package com.studycircle.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.studycircle.android.navigation.StudyCircleNavGraph
import com.studycircle.android.ui.theme.StudyCircleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyCircleTheme {
                // RTL is the default layout direction, not a later polish
                // pass — decided in the UI/UX discussion since the whole app
                // (terminology, task content) is Arabic-first.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    StudyCircleNavGraph()
                }
            }
        }
    }
}
