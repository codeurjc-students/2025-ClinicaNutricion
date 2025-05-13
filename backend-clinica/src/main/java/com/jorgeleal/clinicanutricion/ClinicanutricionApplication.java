package com.jorgeleal.clinicanutricion;

import org.springframework.boot.SpringApplication;
import com.jorgeleal.clinicanutricion.config.EnvLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ClinicanutricionApplication {
	public static void main(String[] args) {
		EnvLoader.loadEnv();
		SpringApplication.run(ClinicanutricionApplication.class, args);
	}

}
