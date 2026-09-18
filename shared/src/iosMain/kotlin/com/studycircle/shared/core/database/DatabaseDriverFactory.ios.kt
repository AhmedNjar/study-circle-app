package com.studycircle.shared.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.studycircle.shared.database.StudyCircleDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(StudyCircleDatabase.Schema, "study_circle.db")
}
