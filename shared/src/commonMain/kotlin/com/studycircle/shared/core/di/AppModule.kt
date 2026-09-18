package com.studycircle.shared.core.di

import com.studycircle.shared.core.database.DatabaseDriverFactory
import com.studycircle.shared.core.network.HttpClientFactory
import com.studycircle.shared.core.network.TokenProvider
import com.studycircle.shared.database.StudyCircleDatabase
import com.studycircle.shared.features.auth.di.authModule
import com.studycircle.shared.features.circle.di.circleModule
import com.studycircle.shared.features.task.di.taskModule
import org.koin.dsl.module

val coreModule = module {
    single { get<DatabaseDriverFactory>().createDriver() }
    single { StudyCircleDatabase(get()) }
    // TokenProvider is resolved here but implemented in authModule
    // (SessionLocalDataSource) — see TokenProvider.kt for why.
    single { HttpClientFactory.create(get<TokenProvider>()) }
}

val appModules = listOf(coreModule, authModule, circleModule, taskModule)
