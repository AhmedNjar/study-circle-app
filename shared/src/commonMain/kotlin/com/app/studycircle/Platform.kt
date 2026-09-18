package com.app.studycircle

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform