package org.example.twitter;

import org.springframework.boot.SpringApplication;

public class TestTwitterApplication {

	public static void main(String[] args) {
		SpringApplication.from(TwitterApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
