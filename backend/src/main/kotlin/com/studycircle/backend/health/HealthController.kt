package com.studycircle.backend.health

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class HealthController {
    // Phase 0 deliverable: this is the "backend responds" checkpoint from
    // the implementation plan. Auth, circle, task, submission and media
    // controllers land in their own packages as each phase is built.
    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf("status" to "ok")
}
