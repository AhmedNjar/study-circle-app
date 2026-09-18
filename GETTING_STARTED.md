# المحفِظ والمُتعلِّم — Phase 0 + 1 + 2 + 3 + 4 scaffold

Foundation, auth + circle, the task lifecycle, streak calculation, and now group tasks: a supervisor can create a "Groups" task, students form or join a group, each member submits their own evidence, and once the group's completion rule is satisfied the whole group's collective الرِّباط advances.

## Important caveat
Still a sandbox, still no `./gradlew build` run here. Phase 4 touches the most existing files of any phase so far (every sealed-class "Phase 4 adds X here" comment scattered through Phases 0–3 finally got filled in) — worth a careful first local build.

## Prerequisites
Unchanged.

## Running each piece
- **Backend**, new endpoints:
  - `POST /api/v1/tasks/{taskId}/groups` — create (`{"name": "..."}`) or join (`{"groupId": "..."}`) a group (STUDENT)
  - `GET /api/v1/tasks/{taskId}/groups` — list groups formed for a task
  - Submitting evidence now accepts `"submitter": {"type": "GROUP", "groupId": "...", "submittedByUserId": "..."}` alongside the existing `"INDIVIDUAL"` shape
  - A task's `GET` response now resolves `audience.groupIds` live from the database rather than trusting whatever was stored at creation — see the comment in `TaskService.kt`
- **Android app**: Create Task now has an "المشاركون" (participants) picker — الكل vs. مجموعات. Opening a group task in the taskrunner shows a join/create step first; once in a group, the screen shows the group's own الرِّباط badge alongside the usual timer/recorder. The review queue now shows who (or which group + member) each submission came from.

## What's deliberately still missing (by design, not oversight)
- **All earlier phases' gaps still apply** (role-aware nav, token refresh, manual sync trigger, iOS shell, UTC-only streak day boundaries, etc.) — unchanged.
- **Every group defaults to `ALL_MUST_SUBMIT`.** A supervisor can choose Groups as the audience, but not yet choose the completion rule per task — `TaskGroupService.formOrJoin()` hardcodes it. Letting the supervisor pick `ANY_SUFFICES` at task-creation time is a real next step, not a redesign — the domain model, backend enum, and streak-completion logic already handle both rules correctly, there's just no UI to select the other one yet.
- **No circle-membership re-check on group join.** `TaskGroupService.formOrJoin()` verifies the task is actually a Groups-audience task, but doesn't re-verify the joining student is a member of the task's circle. In practice they'd need to already know the task id (gated behind `GET /circles/{id}/tasks`, which does check membership) — not airtight, but consistent with the level of rigor elsewhere; flagged rather than silently assumed safe.
- **Submitter identity in the review queue is a raw user id**, not a resolved display name — `ReviewQueueScreen.kt`'s own comment explains why (it doesn't currently load circle member profiles the way Circle Detail does).
- **`CreateTaskScreen`'s "GROUPS" choice still only sets `TaskAudience.Groups(emptyList())`** at creation — no way to pre-assign students to groups from the supervisor side; every group is student-initiated (form or join, never assigned).

## Directory map
`features/task` gains `TaskGroupRepository`/`TaskGroupApi`/`TaskGroupRepositoryImpl` and the two group use cases alongside the existing task/submission stack. New backend files live in the existing `task` package (`TaskGroupEntity`, `TaskGroupRepository`, `TaskGroupService`, `TaskGroupController`) rather than a new top-level package, since a task group is conceptually part of the task feature, not its own domain.



