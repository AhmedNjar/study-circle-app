package com.studycircle.android.ui.mvi

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CoroutineScope

// The shared MviViewModel base is deliberately androidx-free (so it works in
// commonMain / iOS), which means on its own it has no lifecycle: screens used to
// build it with `remember { VM(..., rememberCoroutineScope()) }`. That ties the
// ViewModel to the *composition*, so every rotation / process-config change threw
// away its state (a half-filled register form, a task timer mid-run, ...).
//
// This holder bridges the two worlds: a thin androidx ViewModel owns the shared
// MviViewModel and hands it `viewModelScope`. The holder is scoped to the current
// ViewModelStoreOwner — inside a NavHost destination that's the NavBackStackEntry —
// so state survives configuration changes and is cleared when the destination is
// popped. No change to any commonMain code.
class MviViewModelHolder<T : Any>(create: (CoroutineScope) -> T) : ViewModel() {
    val instance: T = create(viewModelScope)
}

// [key] must distinguish instances that take constructor parameters (e.g. a task
// id), otherwise two different ids on the same back-stack entry would share one VM.
@Composable
inline fun <reified T : Any> rememberMviViewModel(
    key: String? = null,
    crossinline create: (CoroutineScope) -> T
): T {
    val holder = viewModel<MviViewModelHolder<T>>(
        key = (T::class.qualifiedName ?: "mvi") + (key?.let { ":$it" } ?: ""),
        factory = viewModelFactory {
            initializer { MviViewModelHolder { scope -> create(scope) } }
        }
    )
    return holder.instance
}
