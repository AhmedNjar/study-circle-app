package com.studycircle.backend.task

import com.studycircle.backend.security.CurrentUser
import com.studycircle.backend.task.dto.CreateTaskRequest
import com.studycircle.backend.task.dto.TaskResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/circles/{circleId}/tasks")
class TaskController(private val taskService: TaskService) {

    @PostMapping
    @PreAuthorize("hasRole('SUPERVISOR')")
    fun create(@PathVariable circleId: String, @RequestBody request: CreateTaskRequest): ResponseEntity<TaskResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(circleId, request, CurrentUser.get().userId))

    @GetMapping
    fun list(@PathVariable circleId: String): ResponseEntity<List<TaskResponse>> =
        ResponseEntity.ok(taskService.listByCircle(circleId, CurrentUser.get().userId))
}
