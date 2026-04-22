package com.invoicesystem.service;

import com.invoicesystem.model.Invoice;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationService {

    private final TemplateEngine templateEngine;

    @Value("${app.pdf.storage-path:./pdfs}")
    private String pdfStoragePath;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public String generateInvoicePdf(Invoice invoice) {
        try {
            Path storagePath = Paths.get(pdfStoragePath);
            Files.createDirectories(storagePath);

            String filename = invoice.getInvoiceNumber() + ".pdf";
            Path pdfPath = storagePath.resolve(filename);

            String htmlContent = generateHtml(invoice);
            byte[] pdfBytes = convertHtmlToPdf(htmlContent);

            try (FileOutputStream fos = new FileOutputStream(pdfPath.toFile())) {
                fos.write(pdfBytes);
            }

            log.info("PDF generated: {}", pdfPath.toAbsolutePath());
            return pdfPath.toAbsolutePath().toString();

        } catch (IOException e) {
            log.error("Failed to generate PDF for invoice {}: {}", invoice.getInvoiceNumber(), e.getMessage());
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    public byte[] generateInvoicePdfBytes(Invoice invoice) {
        String htmlContent = generateHtml(invoice);
        return convertHtmlToPdf(htmlContent);
    }

    private String generateHtml(Invoice invoice) {
        Context context = new Context();
        context.setVariable("invoice", invoice);
        context.setVariable("dateFormat", DATE_FORMAT);
        context.setVariable("issuer", invoice.getIssuedBy());
        context.setVariable("client", invoice.getClient());
        context.setVariable("items", invoice.getItems());
        return templateEngine.process("invoice/pdf-template", context);
    }

    private byte[] convertHtmlToPdf(String html) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ConverterProperties properties = new ConverterProperties();
            HtmlConverter.convertToPdf(html, baos, properties);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("HTML to PDF conversion failed", e);
        }
    }
}
