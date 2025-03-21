package com.jorgeleal.clinicanutricion.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {
    public static void loadEnv() {
        String env = System.getenv("APP_ENV");
        if (env == null) {
            Dotenv dotenv = Dotenv.load();
            dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        } else {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
        }
    }
}
