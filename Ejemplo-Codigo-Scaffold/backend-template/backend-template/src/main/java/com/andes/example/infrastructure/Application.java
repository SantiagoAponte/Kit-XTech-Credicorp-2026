package com.andes.example.infrastructure;

import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

/** Arranque. La clase vive en infrastructure, por eso se declara el paquete base explícito. */
@SpringBootApplication(scanBasePackages = "com.andes.example")
@AutoConfigurationPackage(basePackages = "com.andes.example")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
