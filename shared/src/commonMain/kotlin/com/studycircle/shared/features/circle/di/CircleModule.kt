package com.studycircle.shared.features.circle.di

import com.studycircle.shared.features.circle.data.remote.CircleApi
import com.studycircle.shared.features.circle.data.repository.CircleRepositoryImpl
import com.studycircle.shared.features.circle.domain.repository.CircleRepository
import com.studycircle.shared.features.circle.domain.usecase.CreateCircleUseCase
import com.studycircle.shared.features.circle.domain.usecase.GetCircleUseCase
import com.studycircle.shared.features.circle.domain.usecase.JoinCircleUseCase
import org.koin.dsl.module

val circleModule = module {
    single { CircleApi(get()) }
    single<CircleRepository> { CircleRepositoryImpl(get(), get()) }
    factory { CreateCircleUseCase(get()) }
    factory { JoinCircleUseCase(get()) }
    factory { GetCircleUseCase(get()) }
}
