package com.studycircle.backend.streak

import com.studycircle.backend.streak.dto.StreakResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/circles/{circleId}/streaks")
class StreakController(private val streakService: StreakService) {
    // No @PreAuthorize role restriction — any authenticated circle member
    // can look up any streak in a circle they're part of. Phase 2's pattern
    // of an explicit membership check (like CircleService.getDetail) would
    // be the next hardening step here if streaks need to stay private
    // between students; not enforced yet.
    @GetMapping
    fun get(@PathVariable circleId: String, @RequestParam owner: String): ResponseEntity<StreakResponse> =
        ResponseEntity.ok(streakService.getForOwner(circleId, owner))
}
