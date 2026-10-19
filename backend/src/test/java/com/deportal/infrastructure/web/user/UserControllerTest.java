package com.deportal.infrastructure.web.user;

import java.util.List;

import com.deportal.application.auth.AuthenticatedUser;
import com.deportal.application.user.ChangeMembership;
import com.deportal.application.user.ListCustomers;
import com.deportal.domain.user.failure.CustomerNotFound;
import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserAccount;
import com.deportal.domain.user.UserRole;
import com.deportal.infrastructure.security.SecurityConfig;
import com.deportal.infrastructure.security.TokenIssuer;
import com.deportal.infrastructure.web.auth.AuthExceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({ SecurityConfig.class, TokenIssuer.class, UserExceptionHandler.class, AuthExceptionHandler.class })
@TestPropertySource(properties = {
		"jwt.secret=0123456789abcdef0123456789abcdef",
		"jwt.issuer=deportal",
		"jwt.expiration-seconds=900"
})
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TokenIssuer tokens;

	@MockitoBean
	private ListCustomers listCustomers;

	@MockitoBean
	private ChangeMembership changeMembership;

	@Test
	void listsCustomers() throws Exception {
		when(listCustomers.execute()).thenReturn(List.of(customer(MembershipType.NON_MEMBER)));

		mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(UserRole.ADMIN)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(2))
			.andExpect(jsonPath("$[0].name").value("Ana"))
			.andExpect(jsonPath("$[0].email").value("ana@deportal.test"))
			.andExpect(jsonPath("$[0].membership").value("NON_MEMBER"))
			.andExpect(jsonPath("$[0].passwordHash").doesNotExist())
			.andExpect(jsonPath("$[0].role").doesNotExist());
	}

	@Test
	void changesMembership() throws Exception {
		when(changeMembership.execute(2L, MembershipType.MEMBER)).thenReturn(customer(MembershipType.MEMBER));

		mockMvc.perform(put("/api/admin/users/2").header("Authorization", bearer(UserRole.ADMIN))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"membership\":\"MEMBER\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.membership").value("MEMBER"));
	}

	@Test
	void rejectsAnUnknownMembership() throws Exception {
		mockMvc.perform(put("/api/admin/users/2").header("Authorization", bearer(UserRole.ADMIN))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"membership\":\"VIP\"}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void rejectsACustomer() throws Exception {
		mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(UserRole.CUSTOMER)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.detail").value("Admin access required"));
	}

	@Test
	void reportsAMissingCustomer() throws Exception {
		when(changeMembership.execute(9L, MembershipType.MEMBER)).thenThrow(new CustomerNotFound());

		mockMvc.perform(put("/api/admin/users/9").header("Authorization", bearer(UserRole.ADMIN))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"membership\":\"MEMBER\"}"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Customer not found"));
	}

	private String bearer(UserRole role) {
		String token = tokens
			.issue(new AuthenticatedUser(1L, "Admin", "admin@admin.com", role, MembershipType.NON_MEMBER))
			.value();
		return "Bearer " + token;
	}

	private static UserAccount customer(MembershipType type) {
		return new UserAccount(2L, "Ana", "ana@deportal.test", "hash", UserRole.CUSTOMER, type, false);
	}

}
