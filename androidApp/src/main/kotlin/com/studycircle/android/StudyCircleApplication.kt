package com.studycircle.android

import android.app.Application
import com.studycircle.shared.core.database.DatabaseDriverFactory
import com.studycircle.shared.core.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class StudyCircleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@StudyCircleApplication)
            // DatabaseDriverFactory's Android actual needs a Context, which is
            // platform-specific wiring — it can't live in the KMP-shared
            // coreModule (commonMain doesn't know about android.content.Context).
            // The iOS entry point will need its own equivalent single{} for the
            // no-arg iOS actual once that target has a real app shell.
            modules(appModules + module { single { DatabaseDriverFactory(get()) } })
        }
    }
}
