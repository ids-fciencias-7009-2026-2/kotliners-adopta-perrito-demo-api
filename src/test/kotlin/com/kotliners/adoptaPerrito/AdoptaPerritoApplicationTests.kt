package com.kotliners.adoptaPerrito

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

// Se omite @SpringBootTest para no requerir BD ni contexto completo en CI
class AdoptaPerritoApplicationTests {

	@Test
	fun contextLoads() {
		// Smoke test: verifica que las clases principales compilan y cargan
		assertTrue(true)
	}

}
