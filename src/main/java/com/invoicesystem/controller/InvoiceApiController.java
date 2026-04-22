package com.invoicesystem.controller;

import com.invoicesystem.dto.InvoiceDto;
import com.invoicesystem.model.Invoice;
import com.invoicesystem.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceApiController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping
    public List<Invoice> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @PostMapping
    public Invoice createInvoice(@RequestBody InvoiceDto.Request dto) {
        return invoiceService.createInvoice(dto);
    }
}
