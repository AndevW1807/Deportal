package com.deportal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DeportalApplicationTests {

	@Test
	void applicationClassLoadsWithoutDatabase() {
		assertDoesNotThrow(() -> Class.forName("com.deportal.DeportalApplication"));
	}

}
