package com.invoicesystem.repository;

import com.invoicesystem.model.Invoice;
import com.invoicesystem.model.Invoice.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findByIssuedById(Long userId, Pageable pageable);

    Page<Invoice> findByIssuedByIdAndStatus(Long userId, InvoiceStatus status, Pageable pageable);

    List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate date);

    List<Invoice> findByRecurringTrueAndNextRecurrenceDateLessThanEqual(LocalDate date);

    List<Invoice> findByStatusAndReminderSentFalseAndDueDateBefore(InvoiceStatus status, LocalDate date);

    @Query("SELECT i FROM Invoice i WHERE i.issuedBy.id = :userId AND " +
           "(LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.client.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Invoice> searchInvoices(@Param("userId") Long userId,
                                  @Param("query") String query,
                                  Pageable pageable);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.issuedBy.id = :userId AND i.status = :status")
    BigDecimal sumTotalByUserAndStatus(@Param("userId") Long userId, @Param("status") InvoiceStatus status);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.issuedBy.id = :userId AND i.status = :status")
    Long countByUserAndStatus(@Param("userId") Long userId, @Param("status") InvoiceStatus status);

    @Query("SELECT MAX(i.invoiceNumber) FROM Invoice i WHERE i.invoiceNumber LIKE :prefix%")
    Optional<String> findMaxInvoiceNumberWithPrefix(@Param("prefix") String prefix);

    @Modifying
    @Query("UPDATE Invoice i SET i.status = 'OVERDUE' WHERE i.status = 'SENT' AND i.dueDate < :today")
    int markOverdueInvoices(@Param("today") LocalDate today);
}
