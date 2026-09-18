plugins {
    kotlin("multiplatform") version "2.0.20" apply false
    kotlin("android") version "2.0.20" apply false
    kotlin("jvm") version "2.0.20" apply false
    kotlin("plugin.serialization") version "2.0.20" apply false
    kotlin("plugin.spring") version "2.0.20" apply false
    id("com.android.application") version "8.5.2" apply false
    id("com.android.library") version "8.5.2" apply false
    id("app.cash.sqldelight") version "2.0.2" apply false
    id("org.springframework.boot") version "3.3.3" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    kotlin("plugin.compose") version "2.0.20" apply false
    id("org.jetbrains.compose") version "1.6.11" apply false
}
