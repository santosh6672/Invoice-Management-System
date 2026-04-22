package com.invoicesystem.service;

import com.invoicesystem.model.Invoice;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final PdfGenerationService pdfGenerationService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.company.name:Invoice System}")
    private String companyName;

    @Async
    public void sendInvoiceEmail(Invoice invoice) {
        try {
            String subject = String.format("Invoice %s from %s",
                    invoice.getInvoiceNumber(),
                    invoice.getIssuedBy().getCompanyName() != null
                            ? invoice.getIssuedBy().getCompanyName()
                            : invoice.getIssuedBy().getFullName());

            String htmlContent = buildEmailContent("email/invoice-email", invoice,
                    "Please find your invoice attached.");

            byte[] pdfBytes = pdfGenerationService.generateInvoicePdfBytes(invoice);

            sendHtmlEmailWithAttachment(
                    invoice.getClient().getEmail(),
                    subject,
                    htmlContent,
                    pdfBytes,
                    invoice.getInvoiceNumber() + ".pdf"
            );

            log.info("Invoice email sent to: {}", invoice.getClient().getEmail());

        } catch (Exception e) {
            log.error("Failed to send invoice email for {}: {}", invoice.getInvoiceNumber(), e.getMessage());
        }
    }

    @Async
    public void sendPaymentConfirmation(Invoice invoice) {
        try {
            String subject = String.format("Payment Received - Invoice %s", invoice.getInvoiceNumber());
            String htmlContent = buildEmailContent("email/payment-confirmation", invoice,
                    "Your payment has been received. Thank you!");

            sendHtmlEmail(invoice.getClient().getEmail(), subject, htmlContent);
            log.info("Payment confirmation sent to: {}", invoice.getClient().getEmail());

        } catch (Exception e) {
            log.error("Failed to send payment confirmation for {}: {}", invoice.getInvoiceNumber(), e.getMessage());
        }
    }

    @Async
    public void sendPaymentReminder(Invoice invoice) {
        try {
            String subject = String.format("Payment Reminder - Invoice %s Due Soon", invoice.getInvoiceNumber());
            String htmlContent = buildEmailContent("email/payment-reminder", invoice,
                    "This is a friendly reminder that your invoice is due soon.");

            sendHtmlEmail(invoice.getClient().getEmail(), subject, htmlContent);
            log.info("Payment reminder sent to: {}", invoice.getClient().getEmail());

        } catch (Exception e) {
            log.error("Failed to send payment reminder for {}: {}", invoice.getInvoiceNumber(), e.getMessage());
        }
    }

    @Async
    public void sendOverdueNotification(Invoice invoice) {
        try {
            String subject = String.format("OVERDUE: Invoice %s - Immediate Action Required", invoice.getInvoiceNumber());
            String htmlContent = buildEmailContent("email/overdue-notification", invoice,
                    "Your invoice is overdue. Please arrange payment immediately.");

            sendHtmlEmail(invoice.getClient().getEmail(), subject, htmlContent);
            log.info("Overdue notification sent to: {}", invoice.getClient().getEmail());

        } catch (Exception e) {
            log.error("Failed to send overdue notification for {}: {}", invoice.getInvoiceNumber(), e.getMessage());
        }
    }

    private String buildEmailContent(String template, Invoice invoice, String message) {
        Context context = new Context();
        context.setVariable("invoice", invoice);
        context.setVariable("client", invoice.getClient());
        context.setVariable("issuer", invoice.getIssuedBy());
        context.setVariable("message", message);
        context.setVariable("companyName", companyName);
        return templateEngine.process(template, context);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    private void sendHtmlEmailWithAttachment(String to, String subject, String htmlContent,
                                              byte[] attachment, String attachmentName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.addAttachment(attachmentName,
                () -> new java.io.ByteArrayInputStream(attachment));
        mailSender.send(message);
    }
}
