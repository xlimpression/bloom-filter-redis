package com.singhand.bloomFilter.runner;

import com.singhand.bloomFilter.model.Role;
import com.singhand.bloomFilter.model.User;
import com.singhand.bloomFilter.service.AuthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class QueryCommandLineRunner implements CommandLineRunner {

	private static final Logger LOGGER = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

	private AuthService authService;

	public QueryCommandLineRunner(AuthService authService) {
		this.authService = authService;
	}

	@Override
	public void run(String... args) throws Exception {
	}


}
