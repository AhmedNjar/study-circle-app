package com.studycircle.shared.features.home.di

import com.studycircle.shared.features.home.domain.usecase.GetHomeDashboardUseCase
import org.koin.dsl.module

val homeModule = module {
    factory { GetHomeDashboardUseCase(get(), get(), get(), get(), get(), get()) }
}
