package com.studycircle.shared.features.task.di

import com.studycircle.shared.features.auth.domain.repository.AuthRepository
import com.studycircle.shared.features.task.data.remote.MediaApi
import com.studycircle.shared.features.task.data.remote.SubmissionApi
import com.studycircle.shared.features.task.data.remote.TaskApi
import com.studycircle.shared.features.task.data.repository.SubmissionRepositoryImpl
import com.studycircle.shared.features.task.data.repository.TaskRepositoryImpl
import com.studycircle.shared.features.task.domain.repository.SubmissionRepository
import com.studycircle.shared.features.task.domain.repository.TaskRepository
import com.studycircle.shared.features.task.domain.usecase.CreateTaskUseCase
import com.studycircle.shared.features.task.domain.usecase.GetTaskUseCase
import com.studycircle.shared.features.task.domain.usecase.ListSubmissionsUseCase
import com.studycircle.shared.features.task.domain.usecase.ListTasksUseCase
import com.studycircle.shared.features.task.domain.usecase.ReviewSubmissionUseCase
import com.studycircle.shared.features.task.domain.usecase.SubmitEvidenceUseCase
import org.koin.dsl.module

val taskModule = module {
    single { TaskApi(get()) }
    single { SubmissionApi(get()) }
    single { MediaApi(get()) }
    single<TaskRepository> { TaskRepositoryImpl(get(), get()) }
    single<SubmissionRepository> {
        // A lambda into AuthRepository, not a direct constructor dependency —
        // the submission repo only needs "who's logged in", not auth's full
        // surface (login/register/etc).
        SubmissionRepositoryImpl(get(), get(), get(), currentUserId = { get<AuthRepository>().currentSession()?.userId })
    }
    factory { CreateTaskUseCase(get()) }
    factory { ListTasksUseCase(get()) }
    factory { GetTaskUseCase(get()) }
    factory { SubmitEvidenceUseCase(get()) }
    factory { ListSubmissionsUseCase(get()) }
    factory { ReviewSubmissionUseCase(get()) }
}
