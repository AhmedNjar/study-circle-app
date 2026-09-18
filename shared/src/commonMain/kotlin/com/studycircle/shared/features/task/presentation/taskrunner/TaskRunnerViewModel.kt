package com.studycircle.shared.features.task.presentation.taskrunner

import com.studycircle.shared.core.mvi.MviViewModel
import com.studycircle.shared.features.auth.domain.repository.AuthRepository
import com.studycircle.shared.features.task.domain.model.MediaType
import com.studycircle.shared.features.task.domain.model.SubmissionEvidence
import com.studycircle.shared.features.task.domain.model.TaskAudience
import com.studycircle.shared.features.task.domain.model.TrackingMethod
import com.studycircle.shared.features.task.domain.usecase.EvidenceValidationResult
import com.studycircle.shared.features.task.domain.usecase.FormOrJoinTaskGroupUseCase
import com.studycircle.shared.features.task.domain.usecase.GetTaskUseCase
import com.studycircle.shared.features.task.domain.usecase.ListTaskGroupsUseCase
import com.studycircle.shared.features.task.domain.usecase.SubmitEvidenceUseCase
import com.studycircle.shared.features.task.domain.usecase.ValidateEvidenceUseCase
import kotlinx.coroutines.CoroutineScope

class TaskRunnerViewModel(
    private val getTaskUseCase: GetTaskUseCase,
    private val submitEvidenceUseCase: SubmitEvidenceUseCase,
    private val listTaskGroupsUseCase: ListTaskGroupsUseCase,
    private val formOrJoinTaskGroupUseCase: FormOrJoinTaskGroupUseCase,
    private val authRepository: AuthRepository,
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
            is TaskRunnerIntent.NewGroupNameChanged -> setState { copy(newGroupName = intent.value, errorMessage = null) }
            TaskRunnerIntent.CreateGroup -> createGroup()
            is TaskRunnerIntent.JoinGroup -> joinGroup(intent.groupId)
            TaskRunnerIntent.Submit -> submit()
        }
    }

    private suspend fun load(taskId: String) {
        setState { copy(isLoading = true, errorMessage = null) }
        getTaskUseCase(taskId).fold(
            onSuccess = { task ->
                if (task == null) {
                    setState { copy(isLoading = false, errorMessage = "المهمة غير موجودة") }
                } else {
                    setState { copy(isLoading = false, task = task) }
                    if (task.audience is TaskAudience.Groups) loadGroupContext(taskId)
                }
            },
            onFailure = { setState { copy(isLoading = false, errorMessage = "تعذر تحميل المهمة") } }
        )
    }

    private suspend fun loadGroupContext(taskId: String) {
        val userId = authRepository.currentSession()?.userId ?: return
        listTaskGroupsUseCase(taskId).onSuccess { groups ->
            val mine = groups.find { userId in it.memberIds }
            setState { copy(availableGroups = groups, myGroup = mine) }
        }
    }

    private suspend fun createGroup() {
        val task = currentState.task ?: return
        formOrJoinTaskGroupUseCase(task.id, groupId = null, name = currentState.newGroupName.ifBlank { null }).fold(
            onSuccess = { group -> setState { copy(myGroup = group, availableGroups = availableGroups + group, newGroupName = "") } },
            onFailure = { setState { copy(errorMessage = "تعذر إنشاء المجموعة") } }
        )
    }

    private suspend fun joinGroup(groupId: String) {
        val task = currentState.task ?: return
        formOrJoinTaskGroupUseCase(task.id, groupId = groupId, name = null).fold(
            onSuccess = { group -> setState { copy(myGroup = group) } },
            onFailure = { setState { copy(errorMessage = "تعذر الانضمام إلى المجموعة") } }
        )
    }

    private suspend fun submit() {
        val task = currentState.task ?: return
        if (task.audience is TaskAudience.Groups && currentState.myGroup == null) {
            setState { copy(errorMessage = "انضم إلى مجموعة أولاً") }
            return
        }
        val evidence = buildEvidence() ?: return

        // Client-side check first (instant feedback); the server enforces
        // the identical rule regardless, since this call can be bypassed.
        val validation = ValidateEvidenceUseCase(task.tracking, evidence)
        if (validation is EvidenceValidationResult.Invalid) {
            setState { copy(errorMessage = validation.reason) }
            return
        }

        setState { copy(isSubmitting = true, errorMessage = null) }
        submitEvidenceUseCase(task.id, evidence, currentState.myGroup?.id).fold(
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
