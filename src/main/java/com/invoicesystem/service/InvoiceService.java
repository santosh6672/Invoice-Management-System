package com.invoicesystem.service;

import com.invoicesystem.dto.InvoiceDto;
import com.invoicesystem.model.*;
import com.invoicesystem.model.Invoice.InvoiceStatus;
import com.invoicesystem.repository.ClientRepository;
import com.invoicesystem.repository.InvoiceRepository;
import com.invoicesystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PdfGenerationService pdfGenerationService;
    private final EmailService emailService;

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice createInvoice(InvoiceDto.Request request) {
        // Default to admin for API calls if no security context is present yet
        return createInvoice(request, "admin");
    }

    public Invoice createInvoice(InvoiceDto.Request request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + request.getClientId()));

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .client(client)
                .issuedBy(user)
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .taxRate(request.getTaxRate())
                .discountAmount(request.getDiscountAmount())
                .currency(request.getCurrency())
                .notes(request.getNotes())
                .terms(request.getTerms())
                .recurring(request.isRecurring())
                .recurrenceInterval(request.getRecurrenceInterval())
                .status(InvoiceStatus.DRAFT)
                .build();

        request.getItems().forEach(itemReq -> {
            InvoiceItem item = InvoiceItem.builder()
                    .description(itemReq.getDescription())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .build();
            item.calculateLineTotal();
            invoice.addItem(item);
        });

        invoice.calculateTotals();

        if (invoice.isRecurring()) {
            invoice.setStatus(InvoiceStatus.RECURRING);
            invoice.setNextRecurrenceDate(calculateNextRecurrenceDate(
                    invoice.getIssueDate(), invoice.getRecurrenceInterval()));
        }

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice created: {}", saved.getInvoiceNumber());
        return saved;
    }

    public Invoice sendInvoice(Long invoiceId, String username) {
        Invoice invoice = getInvoiceForUser(invoiceId, username);

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT invoices can be sent. Current status: " + invoice.getStatus());
        }

        // Generate PDF
        String pdfPath = pdfGenerationService.generateInvoicePdf(invoice);
        invoice.setPdfPath(pdfPath);
        invoice.setStatus(InvoiceStatus.SENT);

        Invoice saved = invoiceRepository.save(invoice);

        // Send email notification
        emailService.sendInvoiceEmail(saved);
        log.info("Invoice sent: {}", saved.getInvoiceNumber());

        return saved;
    }

    public Invoice markAsPaid(Long invoiceId, String username) {
        Invoice invoice = getInvoiceForUser(invoiceId, username);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Invoice is already paid");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot mark a cancelled invoice as paid");
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidDate(LocalDate.now());

        Invoice saved = invoiceRepository.save(invoice);
        emailService.sendPaymentConfirmation(saved);
        log.info("Invoice marked as paid: {}", saved.getInvoiceNumber());

        return saved;
    }

    public Invoice cancelInvoice(Long invoiceId, String username) {
        Invoice invoice = getInvoiceForUser(invoiceId, username);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Cannot cancel a paid invoice");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice cancelled: {}", saved.getInvoiceNumber());
        return saved;
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceForUser(Long id, String username) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + id));

        if (!invoice.getIssuedBy().getUsername().equals(username)) {
            throw new SecurityException("Access denied to invoice: " + id);
        }

        return invoice;
    }

    @Transactional(readOnly = true)
    public Page<Invoice> getInvoicesByUser(String username, InvoiceStatus status, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        if (status != null) {
            return invoiceRepository.findByIssuedByIdAndStatus(user.getId(), status, pageable);
        }
        return invoiceRepository.findByIssuedById(user.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Invoice> searchInvoices(String username, String query, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        return invoiceRepository.searchInvoices(user.getId(), query, pageable);
    }

    @Transactional(readOnly = true)
    public InvoiceDto.DashboardStats getDashboardStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        return InvoiceDto.DashboardStats.builder()
                .totalRevenue(invoiceRepository.sumTotalByUserAndStatus(user.getId(), InvoiceStatus.PAID))
                .pendingAmount(invoiceRepository.sumTotalByUserAndStatus(user.getId(), InvoiceStatus.SENT))
                .overdueAmount(invoiceRepository.sumTotalByUserAndStatus(user.getId(), InvoiceStatus.OVERDUE))
                .totalInvoices(invoiceRepository.countByUserAndStatus(user.getId(), InvoiceStatus.PAID)
                        + invoiceRepository.countByUserAndStatus(user.getId(), InvoiceStatus.SENT))
                .paidCount(invoiceRepository.countByUserAndStatus(user.getId(), InvoiceStatus.PAID))
                .pendingCount(invoiceRepository.countByUserAndStatus(user.getId(), InvoiceStatus.SENT))
                .overdueCount(invoiceRepository.countByUserAndStatus(user.getId(), InvoiceStatus.OVERDUE))
                .draftCount(invoiceRepository.countByUserAndStatus(user.getId(), InvoiceStatus.DRAFT))
                .build();
    }

    public void processOverdueInvoices() {
        int updated = invoiceRepository.markOverdueInvoices(LocalDate.now());
        log.info("Marked {} invoices as overdue", updated);
    }

    public void processRecurringInvoices() {
        List<Invoice> recurringInvoices = invoiceRepository
                .findByRecurringTrueAndNextRecurrenceDateLessThanEqual(LocalDate.now());

        for (Invoice template : recurringInvoices) {
            try {
                createRecurringInvoice(template);
            } catch (Exception e) {
                log.error("Failed to create recurring invoice from template {}: {}", template.getId(), e.getMessage());
            }
        }
    }

    private void createRecurringInvoice(Invoice template) {
        Invoice newInvoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .client(template.getClient())
                .issuedBy(template.getIssuedBy())
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(template.getClient().getPaymentTermsDays()))
                .taxRate(template.getTaxRate())
                .discountAmount(template.getDiscountAmount())
                .currency(template.getCurrency())
                .notes(template.getNotes())
                .terms(template.getTerms())
                .status(InvoiceStatus.DRAFT)
                .build();

        template.getItems().forEach(item -> {
            InvoiceItem newItem = InvoiceItem.builder()
                    .description(item.getDescription())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .sortOrder(item.getSortOrder())
                    .build();
            newItem.calculateLineTotal();
            newInvoice.addItem(newItem);
        });

        newInvoice.calculateTotals();
        Invoice saved = invoiceRepository.save(newInvoice);

        // Update next recurrence date on template
        template.setNextRecurrenceDate(
                calculateNextRecurrenceDate(LocalDate.now(), template.getRecurrenceInterval()));
        invoiceRepository.save(template);

        // Auto-send the new invoice
        String pdfPath = pdfGenerationService.generateInvoicePdf(saved);
        saved.setPdfPath(pdfPath);
        saved.setStatus(InvoiceStatus.SENT);
        invoiceRepository.save(saved);
        emailService.sendInvoiceEmail(saved);

        log.info("Recurring invoice created and sent: {}", saved.getInvoiceNumber());
    }

    public void sendPaymentReminders() {
        LocalDate reminderDate = LocalDate.now().plusDays(3);
        List<Invoice> dueSoonInvoices = invoiceRepository
                .findByStatusAndReminderSentFalseAndDueDateBefore(InvoiceStatus.SENT, reminderDate);

        for (Invoice invoice : dueSoonInvoices) {
            emailService.sendPaymentReminder(invoice);
            invoice.setReminderSent(true);
            invoiceRepository.save(invoice);
        }

        // Also send reminders for overdue invoices
        List<Invoice> overdueInvoices = invoiceRepository
                .findByStatusAndDueDateBefore(InvoiceStatus.OVERDUE, LocalDate.now());

        for (Invoice invoice : overdueInvoices) {
            emailService.sendOverdueNotification(invoice);
        }

        log.info("Sent {} payment reminders, {} overdue notifications",
                dueSoonInvoices.size(), overdueInvoices.size());
    }

    private String generateInvoiceNumber() {
        String prefix = "INV-" + DateTimeFormatter.ofPattern("yyyy").format(LocalDate.now()) + "-";
        return invoiceRepository.findMaxInvoiceNumberWithPrefix(prefix)
                .map(last -> {
                    int seq = Integer.parseInt(last.replace(prefix, "")) + 1;
                    return prefix + String.format("%04d", seq);
                })
                .orElse(prefix + "0001");
    }

    private LocalDate calculateNextRecurrenceDate(LocalDate from, Invoice.RecurrenceInterval interval) {
        return switch (interval) {
            case WEEKLY -> from.plusWeeks(1);
            case MONTHLY -> from.plusMonths(1);
            case QUARTERLY -> from.plusMonths(3);
            case ANNUALLY -> from.plusYears(1);
        };
    }
}
