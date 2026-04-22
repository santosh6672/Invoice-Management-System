package com.invoicesystem.dto;

import com.invoicesystem.model.Invoice.InvoiceStatus;
import com.invoicesystem.model.Invoice.RecurrenceInterval;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class InvoiceDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotNull(message = "Client ID is required")
        private Long clientId;

        @NotNull(message = "Issue date is required")
        private LocalDate issueDate;

        @NotNull(message = "Due date is required")
        @Future(message = "Due date must be in the future")
        private LocalDate dueDate;

        @Valid
        @NotEmpty(message = "At least one item is required")
        private List<ItemRequest> items;

        @DecimalMin(value = "0.0", message = "Tax rate must be positive")
        @DecimalMax(value = "100.0", message = "Tax rate cannot exceed 100%")
        @Builder.Default
        private BigDecimal taxRate = BigDecimal.valueOf(18.0);

        @DecimalMin(value = "0.0", message = "Discount must be positive")
        @Builder.Default
        private BigDecimal discountAmount = BigDecimal.ZERO;

        @Size(max = 3, message = "Currency must be 3 characters")
        @Builder.Default
        private String currency = "USD";

        private String notes;
        private String terms;

        private boolean recurring;
        private RecurrenceInterval recurrenceInterval;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemRequest {

        @NotBlank(message = "Description is required")
        private String description;

        @NotNull
        @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
        private BigDecimal quantity;

        @NotNull
        @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
        private BigDecimal unitPrice;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String invoiceNumber;
        private ClientDto.Summary client;
        private InvoiceStatus status;
        private LocalDate issueDate;
        private LocalDate dueDate;
        private LocalDate paidDate;
        private List<ItemResponse> items;
        private BigDecimal subtotal;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
        private String currency;
        private String notes;
        private String terms;
        private boolean recurring;
        private RecurrenceInterval recurrenceInterval;
        private boolean overdue;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemResponse {
        private Long id;
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
        private Integer sortOrder;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private Long id;
        private String invoiceNumber;
        private String clientName;
        private InvoiceStatus status;
        private LocalDate dueDate;
        private BigDecimal totalAmount;
        private String currency;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardStats {
        private BigDecimal totalRevenue;
        private BigDecimal pendingAmount;
        private BigDecimal overdueAmount;
        private Long totalInvoices;
        private Long paidCount;
        private Long pendingCount;
        private Long overdueCount;
        private Long draftCount;
    }
}
