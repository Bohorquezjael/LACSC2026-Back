package com.innovawebJT.lacsc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.innovawebJT.lacsc.config.LoadConfigs;

@SpringBootApplication
public class LacscApplication {

	public static void main(String[] args) {

		LoadConfigs.init();

		SpringApplication.run(LacscApplication.class, args);
	}

}
