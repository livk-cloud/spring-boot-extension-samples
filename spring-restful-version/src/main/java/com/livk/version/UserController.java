package com.livk.version;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author livk
 */
@ApiVersion("v1")
@RestController
@RequestMapping("/users")
public class UserController {

	@GetMapping("")
	public Object v1() {
		return "User v1";
	}

	@GetMapping("")
	@ApiVersion("v2")
	public Object v2() {
		return "User v2";
	}

}
