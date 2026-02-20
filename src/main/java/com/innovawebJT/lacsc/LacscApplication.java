package com.innovawebJT.lacsc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import com.innovawebJT.lacsc.config.LoadConfigs;

@SpringBootApplication
@EnableAsync
public class LacscApplication {

	public static void main(String[] args) {

		LoadConfigs.init();

		SpringApplication.run(LacscApplication.class, args);
	}

}
