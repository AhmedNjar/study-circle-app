package com.studycircle.backend.task

import org.springframework.data.jpa.repository.JpaRepository

interface TaskRepository : JpaRepository<TaskEntity, String> {
    fun findByCircleId(circleId: String): List<TaskEntity>
}
