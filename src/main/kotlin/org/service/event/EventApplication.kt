package org.service.event

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class EventApplication

fun main(args: Array<String>) {
    runApplication<EventApplication>(*args)
}
