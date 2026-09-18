package com.studycircle.shared.features.task.presentation.taskrunner

import com.studycircle.shared.core.mvi.MviEffect
import com.studycircle.shared.core.mvi.MviIntent
import com.studycircle.shared.core.mvi.MviState
import com.studycircle.shared.features.task.domain.model.CircleTask

sealed interface TaskRunnerIntent : MviIntent {
    data class Load(val taskId: String) : TaskRunnerIntent

    // TimedWithReflection flow
    data object StartTimer : TaskRunnerIntent
    data object StopTimer : TaskRunnerIntent
    data class TimerTicked(val elapsedSeconds: Long) : TaskRunnerIntent
    data class ReflectionChanged(val value: String) : TaskRunnerIntent

    // MediaSubmission flow — the platform layer owns the actual MediaRecorder
    // calls; this contract only tracks the recording's outcome.
    data object StartRecording : TaskRunnerIntent
    data class RecordingFinished(val localPath: String, val durationSeconds: Int) : TaskRunnerIntent
    data object DiscardRecording : TaskRunnerIntent

    data object Submit : TaskRunnerIntent
}

sealed interface RecordingState {
    data object Idle : RecordingState
    data object Recording : RecordingState
    data class Recorded(val localPath: String, val durationSeconds: Int) : RecordingState
}

data class TaskRunnerState(
    val task: CircleTask? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // TimedWithReflection state
    val isTimerRunning: Boolean = false,
    val elapsedSeconds: Long = 0,
    val reflectionText: String = "",

    // MediaSubmission state
    val recordingState: RecordingState = RecordingState.Idle,

    val isSubmitting: Boolean = false
) : MviState

sealed interface TaskRunnerEffect : MviEffect {
    data object Submitted : TaskRunnerEffect
    // The platform screen listens for these to actually drive the wall-clock
    // timer / MediaRecorder — the ViewModel doesn't own a coroutine ticking
    // loop or any platform recording API itself, it just tracks the numbers
    // the platform layer reports back via TimerTicked/RecordingFinished.
    data object StartPlatformTimer : TaskRunnerEffect
    data object StopPlatformTimer : TaskRunnerEffect
    data object StartPlatformRecording : TaskRunnerEffect
}
