package com.jorgeleal.clinicanutricion.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {
    public static void loadEnv() {
        String env = System.getenv("APP_ENV");
        Dotenv dotenv;
        if (env == null) {
            dotenv = Dotenv.load();
            dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        } else {
            dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
        }
    }
}
