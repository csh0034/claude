package com.ask.claude

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ClaudeApplication

fun main(args: Array<String>) {
    runApplication<ClaudeApplication>(*args)
}
