package com.studycircle.backend.common

import org.springframework.http.HttpStatus

// One exception type per error code from the step-2 API contract, so the
// global handler below is a straight mapping to status + code, not a pile of
// if/else per controller.
sealed class ApiException(val status: HttpStatus, val code: String, message: String) : RuntimeException(message)

class UsernameTakenException :
    ApiException(HttpStatus.CONFLICT, "USERNAME_TAKEN", "That username is already registered")

class InvalidCredentialsException :
    ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Username or password is incorrect")

class CircleNotFoundException :
    ApiException(HttpStatus.NOT_FOUND, "CIRCLE_NOT_FOUND", "No circle matches that id or join code")

class AlreadyMemberException :
    ApiException(HttpStatus.CONFLICT, "ALREADY_MEMBER", "You're already a member of this circle")

class CircleInactiveException :
    ApiException(HttpStatus.GONE, "CIRCLE_INACTIVE", "This circle is no longer active")

class NotCircleMemberException :
    ApiException(HttpStatus.FORBIDDEN, "NOT_CIRCLE_MEMBER", "You're not a member of this circle")

class NotCircleSupervisorException :
    ApiException(HttpStatus.FORBIDDEN, "NOT_CIRCLE_SUPERVISOR", "You're not the supervisor of this circle")

class TaskNotFoundException :
    ApiException(HttpStatus.NOT_FOUND, "TASK_NOT_FOUND", "No task matches that id")

class SubmissionNotFoundException :
    ApiException(HttpStatus.NOT_FOUND, "SUBMISSION_NOT_FOUND", "No submission matches that id")

class SubmissionBelowThresholdException :
    ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "SUBMISSION_BELOW_THRESHOLD", "This submission doesn't meet the task's minimum requirements")

class EvidenceTypeMismatchException :
    ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "EVIDENCE_TYPE_MISMATCH", "The submitted evidence doesn't match this task's tracking method")

class InvalidTrackingException :
    ApiException(HttpStatus.BAD_REQUEST, "INVALID_TRACKING", "Count-based tasks need a non-blank unit and a target above zero")

class UnsupportedSubmitterException :
    ApiException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_SUBMITTER", "Unrecognized submitter type")

class SubmitterMismatchException :
    ApiException(HttpStatus.FORBIDDEN, "SUBMITTER_MISMATCH", "You can only submit on your own behalf")

class TaskGroupNotFoundException :
    ApiException(HttpStatus.NOT_FOUND, "TASK_GROUP_NOT_FOUND", "No group matches that id for this task")

class AlreadyInTaskGroupException :
    ApiException(HttpStatus.CONFLICT, "ALREADY_IN_TASK_GROUP", "You're already in a group for this task")

class NotGroupMemberException :
    ApiException(HttpStatus.FORBIDDEN, "NOT_GROUP_MEMBER", "You're not a member of this group")

class TaskNotGroupTaskException :
    ApiException(HttpStatus.BAD_REQUEST, "TASK_NOT_GROUP_TASK", "This task isn't set up for group participation")
