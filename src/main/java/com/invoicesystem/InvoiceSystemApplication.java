package com.invoicesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InvoiceSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvoiceSystemApplication.class, args);
    }
}
