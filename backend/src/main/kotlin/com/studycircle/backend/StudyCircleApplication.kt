package com.studycircle.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StudyCircleApplication

fun main(args: Array<String>) {
    runApplication<StudyCircleApplication>(*args)
}
