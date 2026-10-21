package com.studycircle.backend.circle

import com.studycircle.backend.circle.dto.CircleDetailResponse
import com.studycircle.backend.circle.dto.CircleResponse
import com.studycircle.backend.circle.dto.CreateCircleRequest
import com.studycircle.backend.circle.dto.JoinCircleRequest
import com.studycircle.backend.security.CurrentUser
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/circles")
class CircleController(private val circleService: CircleService) {

    @PostMapping
    @PreAuthorize("hasRole('SUPERVISOR')")
    fun create(@RequestBody request: CreateCircleRequest): ResponseEntity<CircleResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(circleService.create(request.name, CurrentUser.get().userId))

    @PostMapping("/join")
    @PreAuthorize("hasRole('STUDENT')")
    fun join(@RequestBody request: JoinCircleRequest): ResponseEntity<CircleResponse> =
        ResponseEntity.ok(circleService.join(request.joinCode, CurrentUser.get().userId))

    @GetMapping
    fun listMine(): ResponseEntity<List<CircleResponse>> =
        ResponseEntity.ok(circleService.listMine(CurrentUser.get().userId))

    @GetMapping("/{circleId}")
    fun detail(@PathVariable circleId: String): ResponseEntity<CircleDetailResponse> =
        ResponseEntity.ok(circleService.getDetail(circleId, CurrentUser.get().userId))
}
