package com.livk.admin.server;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>
 * SpringServerApp
 * </p>
 *
 * @author livk
 * @date 2026/1/27
 */
@EnableAdminServer
@SpringBootApplication
public class SpringServerApp {

	void main(String[] args) {
		SpringApplication.run(SpringServerApp.class, args);
	}

}
