package com.kotliners.adoptaPerrito

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AdoptaPerritoApplication

fun main(args: Array<String>) {
	runApplication<AdoptaPerritoApplication>(*args)
}
