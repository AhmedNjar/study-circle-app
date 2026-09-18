# المحفِظ والمُتعلِّم — Phase 0 + 1 + 2 scaffold

Foundation, auth + circle, and now the core task lifecycle: a supervisor can create a task (timed-reading or audio-recitation), a student can complete it — offline, if needed — and a supervisor can review and approve/reject it.

## Important caveat
Still written in a sandbox with no Maven Central / Google Maven access, so **I could not run `./gradlew build`**. Run it locally first; I'm glad to fix whatever surfaces. Phase 2 adds more surface area than Phase 1 did (Jackson polymorphic DTOs on the backend, kotlinx.serialization sealed classes on the client, an offline-sync repository, Android's MediaRecorder) — treat this pass as the most likely to need a correction or two.

## Prerequisites
Same as before (JDK 17, Android Studio, Postgres locally) — nothing new except a device/emulator with a microphone if you want to exercise the recitation flow.

## Running each piece
- **Backend**: same as Phase 1, plus now:
  - `POST /api/v1/circles/{circleId}/tasks` — create a task (SUPERVISOR)
  - `GET /api/v1/circles/{circleId}/tasks` — list them
  - `POST /api/v1/tasks/{taskId}/submissions` — submit evidence (STUDENT)
  - `PATCH /api/v1/submissions/{submissionId}` — approve/reject (SUPERVISOR)
  - `POST /api/v1/media/upload-url` + `PUT` the returned URL — local-disk-backed for now (writes to a temp dir), shaped exactly like a real pre-signed-URL flow so swapping in S3/GCS later only touches `MediaController.kt`
- **Android app**: Circle Detail now has a "عرض المهام" button leading to the task list, "+" creates a task, tapping a task opens the taskrunner (timer+reflection or audio recorder, depending on the task), "مراجعة" opens the review queue.
- **Unit tests**: `cd shared && ../gradlew test` runs `ValidateEvidenceUseCaseTest` — the anti-idle-cheating rule, tested without any Android/Spring dependency at all.

## What's deliberately still missing (by design, not oversight)
- **Phase 1's gaps still apply** (no real circle list, no role-aware nav, no token refresh, no iOS shell) — see the Phase 1 notes below, unchanged.
- **Groups and extra activity types** — exactly as planned: `TaskAudience.Groups`, `TrackingMethod.CountBased`, and `ManualCheckIn` all exist in the domain model but nothing in Phase 2 constructs them. That's Phase 4/5.
- **The "sync worker" is manual, not a real background job.** `SubmissionRepository.syncPending()` exists and works, but nothing calls it automatically yet (no WorkManager, no BGTaskScheduler). Wiring it to app-resume or a periodic job is a real Phase 2 polish item — the offline-first *write path* (submit locally first, sync best-effort, retry later) is real and complete; the *trigger* for retrying is not yet automatic.
- **`PlatformFileReader`'s iOS actual is unverified** — it's a best-effort NSData/Kotlin-Native interop implementation with no iOS app shell to run it against. Flagged in its own file comment; sanity-check it first when iOS work starts.
- **Create Task UI is a flat form, not the multi-step wizard** sketched in the UI/UX discussion — and it only targets `TaskAudience.AllMembers`, even though `IndividualMembers` works end-to-end in the domain/data/backend layers already. Both are UI scope lines, not architecture gaps.
- **Streak calculation** — `Submission` approval doesn't touch a streak yet; that's Phase 3, and `SubmissionService.review()` has a `// TODO Phase 3` marking exactly where it plugs in.

## Directory map
`features/task` now has the full data/domain/presentation/di stack, matching the step-1 structure doc. New top-level backend packages: `task`, `submission`, `media`.

