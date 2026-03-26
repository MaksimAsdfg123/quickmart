package com.quickmart

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class QuickmartApplication

fun main(args: Array<String>) {
    runApplication<QuickmartApplication>(*args)
}
