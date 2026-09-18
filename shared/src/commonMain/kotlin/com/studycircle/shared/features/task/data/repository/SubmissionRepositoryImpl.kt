package com.studycircle.shared.features.task.data.repository

import com.studycircle.shared.core.network.safeApiCall
import com.studycircle.shared.core.platform.PlatformFileReader
import com.studycircle.shared.database.StudyCircleDatabase
import com.studycircle.shared.features.task.data.remote.MediaApi
import com.studycircle.shared.features.task.data.remote.SubmissionApi
import com.studycircle.shared.features.task.data.remote.dto.ReviewSubmissionRequestDto
import com.studycircle.shared.features.task.data.remote.dto.SubmissionEvidenceDto
import com.studycircle.shared.features.task.data.remote.dto.SubmitSubmissionRequestDto
import com.studycircle.shared.features.task.data.remote.dto.SubmitterRefDto
import com.studycircle.shared.features.task.data.remote.dto.UploadUrlRequestDto
import com.studycircle.shared.features.task.domain.model.MediaType
import com.studycircle.shared.features.task.domain.model.Submission
import com.studycircle.shared.features.task.domain.model.SubmissionEvidence
import com.studycircle.shared.features.task.domain.model.SubmissionStatus
import com.studycircle.shared.features.task.domain.model.SubmitterRef
import com.studycircle.shared.features.task.domain.repository.SubmissionRepository
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SubmissionRepositoryImpl(
    private val api: SubmissionApi,
    private val mediaApi: MediaApi,
    private val database: StudyCircleDatabase,
    // A lambda, not a direct AuthRepository dependency — the task feature
    // doesn't need to know auth's full surface, just "who's logged in".
    private val currentUserId: suspend () -> String?
) : SubmissionRepository {
    private val queries = database.submissionQueries

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun submit(taskId: String, evidence: SubmissionEvidence): Result<Submission> {
        val userId = currentUserId() ?: return Result.failure(IllegalStateException("No active session"))
        val submission = Submission(
            id = Uuid.random().toString(),
            taskId = taskId,
            submitter = SubmitterRef.Individual(userId),
            submittedAt = Clock.System.now(),
            status = SubmissionStatus.PENDING,
            supervisorNote = null,
            evidence = evidence
        )
        // Written locally FIRST — this is the offline-first guarantee itself,
        // not an optimization. If the app is killed right after this line,
        // the submission still exists and syncPending() will find it later.
        persistLocally(submission, syncedRemotely = false)

        // Best-effort immediate sync. A failure here (offline, upload error)
        // just leaves syncedRemotely = 0 for syncPending() to retry — it is
        // NOT surfaced as a failure of submit() itself, because from the
        // student's perspective the submission succeeded the moment it was
        // logged locally.
        runCatching { pushToServer(submission) }

        return Result.success(submission)
    }

    override suspend fun listByTask(taskId: String, status: SubmissionStatus?): Result<List<Submission>> =
        safeApiCall { api.listByTask(taskId, status?.name) }.map { list -> list.map { it.toDomain() } }

    override suspend fun review(submissionId: String, status: SubmissionStatus, note: String?): Result<Submission> =
        safeApiCall { api.review(submissionId, ReviewSubmissionRequestDto(status.name, note)) }.map { it.toDomain() }

    override suspend fun syncPending(): Result<Int> = runCatching {
        val pending = queries.selectPendingSync().executeAsList()
        var synced = 0
        for (row in pending) {
            val submission = Submission(
                id = row.id, taskId = row.taskId,
                submitter = Json.decodeFromString(SubmitterRefDto.serializer(), row.submitterJson).toDomain(),
                submittedAt = kotlinx.datetime.Instant.parse(row.submittedAt),
                status = SubmissionStatus.valueOf(row.status),
                supervisorNote = row.supervisorNote,
                evidence = Json.decodeFromString(SubmissionEvidenceDto.serializer(), row.evidenceJson).toDomain()
            )
            if (runCatching { pushToServer(submission) }.isSuccess) synced++
        }
        synced
    }

    // Uploads media if needed, submits to the backend, then re-keys the local
    // row under the server's real id and marks it synced — the simplest way
    // to reconcile a client-generated id with the server's without a
    // separate id-mapping table.
    private suspend fun pushToServer(submission: Submission) {
        val readyEvidence = uploadMediaIfNeeded(submission.evidence)
        val response = api.submit(
            submission.taskId,
            SubmitSubmissionRequestDto(submission.submitter.toDto(), readyEvidence.toDto())
        )
        queries.transaction {
            queries.insertOrReplace(
                response.id, submission.taskId,
                Json.encodeToString(SubmitterRefDto.serializer(), response.submitter),
                response.submittedAt, response.status, response.supervisorNote,
                Json.encodeToString(SubmissionEvidenceDto.serializer(), response.evidence),
                1L
            )
            if (response.id != submission.id) queries.deleteById(submission.id)
        }
    }

    private suspend fun uploadMediaIfNeeded(evidence: SubmissionEvidence): SubmissionEvidence {
        if (evidence !is SubmissionEvidence.Media || evidence.remoteMediaUrl != null) return evidence
        val path = evidence.localCachePath ?: error("Media evidence has no local file and no remote URL")
        val bytes = PlatformFileReader.readBytes(path)
        val extension = if (evidence.mediaType == MediaType.AUDIO) "m4a" else "mp4"
        val target = mediaApi.getUploadUrl(UploadUrlRequestDto(evidence.mediaType.name, extension))
        mediaApi.uploadBytes(target.uploadUrl, bytes)
        return evidence.copy(remoteMediaUrl = target.remoteMediaUrl)
    }

    private fun persistLocally(submission: Submission, syncedRemotely: Boolean) {
        queries.insertOrReplace(
            submission.id, submission.taskId,
            Json.encodeToString(SubmitterRefDto.serializer(), submission.submitter.toDto()),
            submission.submittedAt.toString(), submission.status.name, submission.supervisorNote,
            Json.encodeToString(SubmissionEvidenceDto.serializer(), submission.evidence.toDto()),
            if (syncedRemotely) 1L else 0L
        )
    }
}
