package com.jorgeleal.clinicanutricion.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {
    public static void loadEnv() {
        // Inicializa la configuración de variables de entorno para la aplicación.
        String env = System.getenv("APP_ENV");
        if (env == null) {
            Dotenv.load().entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        } else {
            Dotenv.configure().ignoreIfMissing().load();
        }
    }
}
