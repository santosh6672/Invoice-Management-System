package com.invoicesystem.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

public class ClientDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "Client name is required")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        private String name;

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        private String email;

        private String phone;
        private String companyName;
        private String taxNumber;
        private String address;
        private String city;
        private String country;
        private String postalCode;

        @Min(value = 1, message = "Payment terms must be at least 1 day")
        @Max(value = 365, message = "Payment terms cannot exceed 365 days")
        @Builder.Default
        private Integer paymentTermsDays = 30;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String companyName;
        private String taxNumber;
        private String address;
        private String city;
        private String country;
        private String postalCode;
        private Integer paymentTermsDays;
        private boolean active;
        private String fullAddress;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private Long id;
        private String name;
        private String email;
        private String companyName;
    }
}
