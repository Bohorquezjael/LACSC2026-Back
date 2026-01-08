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
        System.setProperty("KEYCLOAK_ISSUER_URI", dotenv.get("KEYCLOAK_ISSUER_URI"));
        System.setProperty("KEYCLOAK_JWK_SET_URI", dotenv.get("KEYCLOAK_JWK_SET_URI"));
        System.setProperty("KEYCLOAK_CLIENT_ID", dotenv.get("KEYCLOAK_CLIENT_ID"));
        System.setProperty("KEYCLOAK_CLIENT_SECRET", dotenv.get("KEYCLOAK_CLIENT_SECRET"));
        System.setProperty("SWAGGER_CLIENT_ID", dotenv.get("SWAGGER_CLIENT_ID"));
        System.setProperty("SWAGGER_CLIENT_SECRET", dotenv.get("SWAGGER_CLIENT_SECRET"));
        System.setProperty("SWAGGER_REALM", dotenv.get("SWAGGER_REALM"));
        System.setProperty("SWAGGER_AUTH_URL", dotenv.get("SWAGGER_AUTH_URL"));
        System.setProperty("SWAGGER_TOKEN_URL", dotenv.get("SWAGGER_TOKEN_URL"));
        System.setProperty("KEYCLOAK_SERVER_URL", dotenv.get("KEYCLOAK_SERVER_URL"));

        System.out.println("✅ Variables cargadas desde .env:");
        System.out.println("DB_URL_DEV = " + System.getProperty("DB_URL_DEV"));
        System.out.println("KEYCLOAK_ISSUER_URI = " + System.getProperty("KEYCLOAK_ISSUER_URI"));
        System.out.println("SWAGGER_CLIENT_ID = " + System.getProperty("SWAGGER_CLIENT_ID"));
    }
}
