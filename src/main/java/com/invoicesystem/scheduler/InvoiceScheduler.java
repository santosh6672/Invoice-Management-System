package com.invoicesystem.scheduler;

import com.invoicesystem.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceScheduler {

    private final InvoiceService invoiceService;

    /**
     * Mark overdue invoices every day at midnight
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void markOverdueInvoices() {
        log.info("Scheduler: Running overdue invoice check...");
        invoiceService.processOverdueInvoices();
    }

    /**
     * Process recurring invoices every day at 6 AM
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void processRecurringInvoices() {
        log.info("Scheduler: Processing recurring invoices...");
        invoiceService.processRecurringInvoices();
    }

    /**
     * Send payment reminders every day at 9 AM
     */
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void sendPaymentReminders() {
        log.info("Scheduler: Sending payment reminders...");
        invoiceService.sendPaymentReminders();
    }

    /**
     * Weekly summary report every Monday at 8 AM
     */
    @Scheduled(cron = "0 0 8 * * MON")
    public void weeklySummary() {
        log.info("Scheduler: Generating weekly summary...");
        // Weekly report can be extended to send summary emails to admins
    }
}
