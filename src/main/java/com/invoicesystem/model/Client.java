package com.invoicesystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "tax_number")
    private String taxNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String city;

    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "payment_terms_days")
    @Builder.Default
    private Integer paymentTermsDays = 30;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Invoice> invoices = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null) sb.append(address);
        if (city != null) sb.append(", ").append(city);
        if (country != null) sb.append(", ").append(country);
        if (postalCode != null) sb.append(" ").append(postalCode);
        return sb.toString();
    }
}
