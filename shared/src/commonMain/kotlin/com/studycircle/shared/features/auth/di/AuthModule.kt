package com.studycircle.shared.features.auth.di

import com.studycircle.shared.core.network.TokenProvider
import com.studycircle.shared.features.auth.data.local.SessionLocalDataSource
import com.studycircle.shared.features.auth.data.remote.AuthApi
import com.studycircle.shared.features.auth.data.repository.AuthRepositoryImpl
import com.studycircle.shared.features.auth.domain.repository.AuthRepository
import com.studycircle.shared.features.auth.domain.usecase.LoginUseCase
import com.studycircle.shared.features.auth.domain.usecase.RegisterUseCase
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    // bind TokenProvider so core's HttpClientFactory can resolve get<TokenProvider>()
    // without ever importing this class directly.
    single { SessionLocalDataSource(get()) } bind TokenProvider::class
    single { AuthApi(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
}
