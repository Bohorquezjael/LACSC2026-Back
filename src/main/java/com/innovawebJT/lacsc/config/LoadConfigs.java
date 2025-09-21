package com.innovawebJT.lacsc.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadConfigs {

    public static void init() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        System.setProperty("DB_URL_DEV", dotenv.get("DB_URL_DEV"));
        System.setProperty("DB_USER_DEV", dotenv.get("DB_USER_DEV"));
        System.setProperty("DB_PASS_DEV", dotenv.get("DB_PASS_DEV"));
        System.setProperty("SPRING_USER", dotenv.get("SPRING_USER"));
        System.setProperty("SPRING_PASS", dotenv.get("SPRING_PASS"));
        System.setProperty("KEYCLOAK_ISSUER_URI", dotenv.get("KEYCLOAK_ISSUER_URI"));
        System.setProperty("KEYCLOAK_JWK_SET_URI", dotenv.get("KEYCLOAK_JWK_SET_URI"));
        System.setProperty("KEYCLOAK_CLIENT_ID", dotenv.get("KEYCLOAK_CLIENT_ID"));

        System.out.println("✅ DB_URL = " + System.getProperty("DB_URL_DEV"));
    }
}
