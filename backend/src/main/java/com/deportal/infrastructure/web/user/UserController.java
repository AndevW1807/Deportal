package com.deportal.infrastructure.web.user;

import java.util.List;

import com.deportal.application.user.ChangeMembership;
import com.deportal.application.user.ListCustomers;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

	private final ListCustomers listCustomers;
	private final ChangeMembership changeMembership;

	public UserController(ListCustomers listCustomers, ChangeMembership changeMembership) {
		this.listCustomers = listCustomers;
		this.changeMembership = changeMembership;
	}

	@GetMapping
	public List<CustomerResponse> users() {
		return listCustomers.execute().stream().map(CustomerResponse::from).toList();
	}

	@PutMapping("/{id}")
	public CustomerResponse change(@PathVariable Long id, @Valid @RequestBody MembershipRequest request) {
		return CustomerResponse.from(changeMembership.execute(id, request.membership()));
	}

}
