package com.studycircle.backend.submission

import org.springframework.data.jpa.repository.JpaRepository

interface SubmissionRepository : JpaRepository<SubmissionEntity, String> {
    fun findByTaskId(taskId: String): List<SubmissionEntity>
    fun findByTaskIdAndStatus(taskId: String, status: SubmissionStatus): List<SubmissionEntity>
}
