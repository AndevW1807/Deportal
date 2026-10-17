package com.deportal.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BcryptPasswordMatcherTest {

	private static final String JUAN_HASH = "$2b$12$DBYyy0c7HVeiNinzBeA9M.Mw.tBgDXWFFR1GUI9NOSko55YXR55uS";

	private final BcryptPasswordMatcher matcher = new BcryptPasswordMatcher();

	@Test
	void acceptsTheSeededDemoPassword() {
		assertThat(matcher.matches("Deportal#2026", JUAN_HASH)).isTrue();
	}

	@Test
	void dummyHashDoesNotMatchTheDemoPassword() {
		assertThat(matcher.matches("Deportal#2026", matcher.dummyHash())).isFalse();
	}

}
