package com.studycircle.shared.core.database

import app.cash.sqldelight.db.SqlDriver

// Platform actuals live in androidMain/iosMain — Android needs a Context for
// its driver, iOS doesn't, which is exactly what expect/actual is for.
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
