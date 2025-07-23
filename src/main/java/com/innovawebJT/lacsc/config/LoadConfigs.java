package com.innovawebJT.lacsc.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadConfigs {

    @PostConstruct
    public void init() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        System.setProperty("DB_URL_DEV", dotenv.get("DB_URL_DEV"));
        System.setProperty("DB_USER_DEV", dotenv.get("DB_USER_DEV"));
        System.setProperty("DB_PASS_DEV", dotenv.get("DB_PASS_DEV"));

        System.out.println("✅ DB_URL = " + System.getProperty("DB_URL_DEV"));
    }
}
