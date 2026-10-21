package com.studycircle.android.ui.theme

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel

// Single owner of the light / dark / follow-system preference.
//   null  = follow system
//   true  = forced dark
//   false = forced light
// Survives rotation (ViewModel) and process death (SharedPreferences) — the previous
// remember{} in MainActivity lost the choice on every configuration change.
class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE)

    private val _isDarkTheme = mutableStateOf(readStored())
    val isDarkTheme: State<Boolean?> = _isDarkTheme

    fun toggleTheme() {
        val next = when (_isDarkTheme.value) {
            true -> false
            false -> null
            null -> true
        }
        _isDarkTheme.value = next
        prefs.edit().apply {
            if (next == null) remove(KEY_DARK) else putBoolean(KEY_DARK, next)
        }.apply()
    }

    private fun readStored(): Boolean? =
        if (prefs.contains(KEY_DARK)) prefs.getBoolean(KEY_DARK, false) else null

    private companion object {
        const val KEY_DARK = "dark_theme"
    }
}
