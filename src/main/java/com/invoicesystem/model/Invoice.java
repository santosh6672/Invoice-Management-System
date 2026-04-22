package com.invoicesystem.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", unique = true, nullable = false)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User issuedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.valueOf(18.0);

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "terms", columnDefinition = "TEXT")
    private String terms;

    @Column(name = "is_recurring")
    @Builder.Default
    private boolean recurring = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_interval")
    private RecurrenceInterval recurrenceInterval;

    @Column(name = "next_recurrence_date")
    private LocalDate nextRecurrenceDate;

    @Column(name = "reminder_sent")
    @Builder.Default
    private boolean reminderSent = false;

    @Column(name = "pdf_path")
    private String pdfPath;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
    }

    public void removeItem(InvoiceItem item) {
        items.remove(item);
        item.setInvoice(null);
    }

    public void calculateTotals() {
        this.subtotal = items.stream()
                .map(InvoiceItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.taxAmount = subtotal
                .multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

        this.totalAmount = subtotal
                .add(taxAmount)
                .subtract(discountAmount);
    }

    public boolean isOverdue() {
        return status == InvoiceStatus.SENT && LocalDate.now().isAfter(dueDate);
    }

    public enum InvoiceStatus {
        DRAFT, SENT, PAID, OVERDUE, CANCELLED, RECURRING
    }

    public enum RecurrenceInterval {
        WEEKLY, MONTHLY, QUARTERLY, ANNUALLY
    }
}
