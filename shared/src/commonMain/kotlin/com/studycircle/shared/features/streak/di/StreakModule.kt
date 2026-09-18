package com.studycircle.shared.features.streak.di

import com.studycircle.shared.features.streak.data.remote.StreakApi
import com.studycircle.shared.features.streak.data.repository.StreakRepositoryImpl
import com.studycircle.shared.features.streak.domain.repository.StreakRepository
import com.studycircle.shared.features.streak.domain.usecase.GetStreakUseCase
import org.koin.dsl.module

val streakModule = module {
    single { StreakApi(get()) }
    single<StreakRepository> { StreakRepositoryImpl(get(), get()) }
    factory { GetStreakUseCase(get()) }
}
