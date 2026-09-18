package com.studycircle.shared.core.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Marker interfaces — every feature's Intent/State/Effect implements one of these.
interface MviIntent
interface MviState
interface MviEffect

// Base ViewModel every feature's presentation layer extends.
// Deliberately framework-light: no androidx.lifecycle dependency, so it stays
// usable from commonMain across Android/iOS. Platform screens wrap the
// CoroutineScope they own (e.g. viewModelScope on Android) and pass it in.
abstract class MviViewModel<Intent : MviIntent, State : MviState, Effect : MviEffect>(
    initialState: State,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    // One-shot events (navigation, snackbars) — not part of State, which should
    // stay idempotent/replayable.
    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 4)
    val effects = _effects.asSharedFlow()

    protected val currentState: State
        get() = _state.value

    fun dispatch(intent: Intent) {
        scope.launch { handleIntent(intent) }
    }

    protected abstract suspend fun handleIntent(intent: Intent)

    protected fun setState(reducer: State.() -> State) {
        _state.value = currentState.reducer()
    }

    protected suspend fun emitEffect(effect: Effect) {
        _effects.emit(effect)
    }
}
