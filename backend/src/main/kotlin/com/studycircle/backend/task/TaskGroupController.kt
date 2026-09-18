package com.studycircle.backend.task

import com.studycircle.backend.security.CurrentUser
import com.studycircle.backend.task.dto.FormOrJoinGroupRequest
import com.studycircle.backend.task.dto.TaskGroupResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/groups")
class TaskGroupController(private val taskGroupService: TaskGroupService) {

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    fun formOrJoin(
        @PathVariable taskId: String,
        @RequestBody request: FormOrJoinGroupRequest
    ): ResponseEntity<TaskGroupResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(taskGroupService.formOrJoin(taskId, request, CurrentUser.get().userId))

    @GetMapping
    fun list(@PathVariable taskId: String): ResponseEntity<List<TaskGroupResponse>> =
        ResponseEntity.ok(taskGroupService.listForTask(taskId))
}
