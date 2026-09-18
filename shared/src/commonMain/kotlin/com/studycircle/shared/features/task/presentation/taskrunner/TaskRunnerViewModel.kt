package com.studycircle.shared.features.task.presentation.taskrunner

import com.studycircle.shared.core.mvi.MviViewModel
import com.studycircle.shared.features.task.domain.model.MediaType
import com.studycircle.shared.features.task.domain.model.SubmissionEvidence
import com.studycircle.shared.features.task.domain.model.TrackingMethod
import com.studycircle.shared.features.task.domain.usecase.EvidenceValidationResult
import com.studycircle.shared.features.task.domain.usecase.GetTaskUseCase
import com.studycircle.shared.features.task.domain.usecase.SubmitEvidenceUseCase
import com.studycircle.shared.features.task.domain.usecase.ValidateEvidenceUseCase
import kotlinx.coroutines.CoroutineScope

class TaskRunnerViewModel(
    private val getTaskUseCase: GetTaskUseCase,
    private val submitEvidenceUseCase: SubmitEvidenceUseCase,
    scope: CoroutineScope
) : MviViewModel<TaskRunnerIntent, TaskRunnerState, TaskRunnerEffect>(TaskRunnerState(), scope) {

    override suspend fun handleIntent(intent: TaskRunnerIntent) {
        when (intent) {
            is TaskRunnerIntent.Load -> load(intent.taskId)
            TaskRunnerIntent.StartTimer -> {
                setState { copy(isTimerRunning = true) }
                emitEffect(TaskRunnerEffect.StartPlatformTimer)
            }
            TaskRunnerIntent.StopTimer -> {
                setState { copy(isTimerRunning = false) }
                emitEffect(TaskRunnerEffect.StopPlatformTimer)
            }
            is TaskRunnerIntent.TimerTicked -> setState { copy(elapsedSeconds = intent.elapsedSeconds) }
            is TaskRunnerIntent.ReflectionChanged -> setState { copy(reflectionText = intent.value, errorMessage = null) }
            TaskRunnerIntent.StartRecording -> {
                setState { copy(recordingState = RecordingState.Recording, errorMessage = null) }
                emitEffect(TaskRunnerEffect.StartPlatformRecording)
            }
            is TaskRunnerIntent.RecordingFinished ->
                setState { copy(recordingState = RecordingState.Recorded(intent.localPath, intent.durationSeconds)) }
            TaskRunnerIntent.DiscardRecording -> setState { copy(recordingState = RecordingState.Idle) }
            TaskRunnerIntent.Submit -> submit()
        }
    }

    private suspend fun load(taskId: String) {
        setState { copy(isLoading = true, errorMessage = null) }
        getTaskUseCase(taskId).fold(
            onSuccess = { task -> setState { copy(isLoading = false, task = task) } },
            onFailure = { setState { copy(isLoading = false, errorMessage = "تعذر تحميل المهمة") } }
        )
    }

    private suspend fun submit() {
        val task = currentState.task ?: return
        val evidence = buildEvidence() ?: return

        // Client-side check first (instant feedback); the server enforces
        // the identical rule regardless, since this call can be bypassed.
        val validation = ValidateEvidenceUseCase(task.tracking, evidence)
        if (validation is EvidenceValidationResult.Invalid) {
            setState { copy(errorMessage = validation.reason) }
            return
        }

        setState { copy(isSubmitting = true, errorMessage = null) }
        submitEvidenceUseCase(task.id, evidence).fold(
            onSuccess = {
                setState { copy(isSubmitting = false) }
                emitEffect(TaskRunnerEffect.Submitted)
            },
            onFailure = { setState { copy(isSubmitting = false, errorMessage = "تعذر إرسال المهمة") } }
        )
    }

    private fun buildEvidence(): SubmissionEvidence? {
        val s = currentState
        return when (s.task?.tracking) {
            is TrackingMethod.TimedWithReflection -> SubmissionEvidence.TimedReflection(
                actualDurationSeconds = s.elapsedSeconds,
                reflectionText = s.reflectionText
            )
            is TrackingMethod.MediaSubmission -> {
                val recorded = s.recordingState as? RecordingState.Recorded
                if (recorded == null) {
                    setState { copy(errorMessage = "سجّل صوتًا أولاً") }
                    null
                } else {
                    SubmissionEvidence.Media(
                        localCachePath = recorded.localPath,
                        remoteMediaUrl = null,
                        mediaType = MediaType.AUDIO,
                        durationSeconds = recorded.durationSeconds
                    )
                }
            }
            else -> null
        }
    }
}
