package com.nisync.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("users")
public class UserController {

	
	@GetMapping
	public String getAllUsers() {
		return "This are all my users";
	}
}
