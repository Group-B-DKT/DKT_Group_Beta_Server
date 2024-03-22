package com.group_beta.dkt_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(JerseyConfig.class)
public class DktServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DktServerApplication.class, args);
	}

}
