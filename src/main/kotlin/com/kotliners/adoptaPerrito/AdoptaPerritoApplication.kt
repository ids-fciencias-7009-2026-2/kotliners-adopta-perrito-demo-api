package com.kotliners.adoptaPerrito

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class AdoptaPerritoApplication

fun main(args: Array<String>) {

dotenv().entries().forEach {
    System.setProperty(it.key, it.value)
}

runApplication<AdoptaPerritoApplication>(*args)
}
