package com.deportal.infrastructure.web.auth;

import com.deportal.application.auth.AuthenticatedUser;
import com.deportal.application.auth.failure.InvalidCredentials;
import com.deportal.application.auth.failure.InvalidRegistration;
import com.deportal.application.auth.login.Login;
import com.deportal.application.auth.register.Register;
import com.deportal.application.auth.session.CurrentUser;
import com.deportal.domain.user.EmailAlreadyUsed;
import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserRole;
import com.deportal.infrastructure.security.SecurityConfig;
import com.deportal.infrastructure.security.TokenIssuer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({ SecurityConfig.class, TokenIssuer.class, AuthExceptionHandler.class })
@TestPropertySource(properties = {
		"jwt.secret=0123456789abcdef0123456789abcdef",
		"jwt.issuer=deportal",
		"jwt.expiration-seconds=900"
})
class AuthControllerTest {

	private static final String PASSWORD = "secret-value";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private Login login;

	@MockitoBean
	private Register register;

	@MockitoBean
	private CurrentUser currentUser;

	@Test
	void loginReturnsABearerTokenWithoutSecrets() throws Exception {
		when(login.execute(anyString(), anyString())).thenReturn(
				new AuthenticatedUser(1L, "Juan Pérez", "juan.perez@deportal.test", UserRole.CUSTOMER,
						MembershipType.MEMBER));

		String body = mockMvc.perform(post("/api/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"email\":\"juan.perez@deportal.test\",\"password\":\"" + PASSWORD + "\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.expiresIn").value(900))
			.andReturn()
			.getResponse()
			.getContentAsString();

		assertNoSecrets(body);
	}

	@Test
	void invalidCredentialsStayGeneric() throws Exception {
		when(login.execute(anyString(), anyString())).thenThrow(new InvalidCredentials());

		String body = mockMvc.perform(post("/api/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"email\":\"missing@deportal.test\",\"password\":\"" + PASSWORD + "\"}"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.detail").value("Invalid credentials"))
			.andReturn()
			.getResponse()
			.getContentAsString();

		assertNoSecrets(body);
	}

	@Test
	void missingBodyIsRejectedWithoutEchoingInput() throws Exception {
		String body = mockMvc.perform(post("/api/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"email\":\"juan.perez@deportal.test\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("Invalid request"))
			.andReturn()
			.getResponse()
			.getContentAsString();

		assertNoSecrets(body);
		org.assertj.core.api.Assertions.assertThat(body).doesNotContain("juan.perez@deportal.test");
	}

	@Test
	void registerCreatesACustomerAndIgnoresARequestedRole() throws Exception {
		when(register.execute(anyString(), anyString(), anyString(), anyString())).thenReturn(
				new AuthenticatedUser(8L, "Ana López", "ana@deportal.test", UserRole.CUSTOMER,
						MembershipType.NON_MEMBER));

		String body = mockMvc.perform(post("/api/auth/register")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"  Ana López  \",\"email\":\"  Ana@Deportal.Test \",\"password\":\"" + PASSWORD
					+ "\",\"confirmPassword\":\"" + PASSWORD + "\",\"role\":\"ADMIN\",\"type\":\"MEMBER\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(8))
			.andExpect(jsonPath("$.name").value("Ana López"))
			.andExpect(jsonPath("$.email").value("ana@deportal.test"))
			.andExpect(jsonPath("$.role").value("CUSTOMER"))
			.andExpect(jsonPath("$.type").value("NON_MEMBER"))
			.andReturn()
			.getResponse()
			.getContentAsString();

		assertNoSecrets(body);
		verify(register).execute("Ana López", "ana@deportal.test", PASSWORD, PASSWORD);
	}

	@Test
	void duplicateEmailIsAConflictWithoutEchoingIt() throws Exception {
		when(register.execute(anyString(), anyString(), anyString(), anyString())).thenThrow(new EmailAlreadyUsed());

		String body = mockMvc.perform(post("/api/auth/register")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"Ana\",\"email\":\"ana@deportal.test\",\"password\":\"" + PASSWORD
					+ "\",\"confirmPassword\":\"" + PASSWORD + "\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.detail").value("Email already registered"))
			.andReturn()
			.getResponse()
			.getContentAsString();

		assertNoSecrets(body);
		org.assertj.core.api.Assertions.assertThat(body).doesNotContain("ana@deportal.test");
	}

	@Test
	void invalidRegistrationStaysGeneric() throws Exception {
		when(register.execute(anyString(), anyString(), anyString(), anyString()))
			.thenThrow(new InvalidRegistration());

		String body = mockMvc.perform(post("/api/auth/register")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"Ana\",\"email\":\"ana@deportal.test\",\"password\":\"" + PASSWORD
					+ "\",\"confirmPassword\":\"other\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("Invalid request"))
			.andReturn()
			.getResponse()
			.getContentAsString();

		assertNoSecrets(body);
		org.assertj.core.api.Assertions.assertThat(body).doesNotContain("ana@deportal.test");
	}

	@Test
	void currentUserRequiresABearerToken() throws Exception {
		String body = mockMvc.perform(get("/api/auth/session"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.detail").value("Invalid credentials"))
			.andReturn()
			.getResponse()
			.getContentAsString();

		assertNoSecrets(body);
	}

	private static void assertNoSecrets(String body) {
		org.assertj.core.api.Assertions.assertThat(body)
			.doesNotContain("password")
			.doesNotContain("passwordHash")
			.doesNotContain(PASSWORD);
	}

}
