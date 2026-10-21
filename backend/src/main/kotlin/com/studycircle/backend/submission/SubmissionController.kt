package com.studycircle.backend.submission

import com.studycircle.backend.security.CurrentUser
import com.studycircle.backend.submission.dto.ReviewSubmissionRequest
import com.studycircle.backend.submission.dto.SubmissionResponse
import com.studycircle.backend.submission.dto.SubmitSubmissionRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class SubmissionController(private val submissionService: SubmissionService) {

    @PostMapping("/tasks/{taskId}/submissions")
    @PreAuthorize("hasRole('STUDENT')")
    fun submit(
        @PathVariable taskId: String,
        @RequestBody request: SubmitSubmissionRequest
    ): ResponseEntity<SubmissionResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(submissionService.submit(taskId, request, CurrentUser.get().userId))

    @GetMapping("/tasks/{taskId}/submissions")
    @PreAuthorize("hasRole('SUPERVISOR')")
    fun list(
        @PathVariable taskId: String,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<List<SubmissionResponse>> =
        ResponseEntity.ok(submissionService.listByTask(taskId, status, CurrentUser.get().userId))

    @PatchMapping("/submissions/{submissionId}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    fun review(
        @PathVariable submissionId: String,
        @RequestBody request: ReviewSubmissionRequest
    ): ResponseEntity<SubmissionResponse> =
        ResponseEntity.ok(submissionService.review(submissionId, request, CurrentUser.get().userId))
}
