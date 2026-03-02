package com.kotliners.adoptaPerrito

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AdoptaPerritoApplication

fun main(args: Array<String>) {
	dotenv().entries().forEach {
		System.setProperty(it.key, it.value)
	}
	runApplication<AdoptaPerritoApplication>(*args)
}
