package com.jorgeleal.clinicanutricion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.jorgeleal.clinicanutricion.config.EnvLoader;

@SpringBootTest
@ActiveProfiles("test")
class ClinicanutricionApplicationTests {
    static {
        EnvLoader.loadEnv();
    }
    
    @Test
    void contextLoads() {
    }
}
