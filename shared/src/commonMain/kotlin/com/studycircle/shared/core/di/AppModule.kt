package com.studycircle.shared.core.di

import com.studycircle.shared.core.database.DatabaseDriverFactory
import com.studycircle.shared.core.network.ApiConfig
import com.studycircle.shared.core.network.HttpClientFactory
import com.studycircle.shared.core.network.TokenProvider
import com.studycircle.shared.database.StudyCircleDatabase
import com.studycircle.shared.features.auth.di.authModule
import com.studycircle.shared.features.circle.di.circleModule
import com.studycircle.shared.features.home.di.homeModule
import com.studycircle.shared.features.streak.di.streakModule
import com.studycircle.shared.features.task.di.taskModule
import org.koin.dsl.module

val coreModule = module {
    single { get<DatabaseDriverFactory>().createDriver() }
    single { StudyCircleDatabase(get()) }
    // TokenProvider is resolved here but implemented in authModule
    // (SessionLocalDataSource) — see TokenProvider.kt for why.
    // ApiConfig is provided by the platform shell (see StudyCircleApplication) —
    // the base URL differs per environment and per platform (Android emulator
    // reaches the host machine at 10.0.2.2, not localhost).
    single { HttpClientFactory.create(get<TokenProvider>(), get<ApiConfig>()) }
}

val appModules = listOf(coreModule, authModule, circleModule, taskModule, streakModule, homeModule)
