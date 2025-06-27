package com.shuaibu.service;

import java.util.List;

import com.shuaibu.dto.InvoiceDto;
import com.shuaibu.model.InvoiceModel;

public interface InvoiceService {
    List<InvoiceDto> getAllInvoices();
    InvoiceDto getInvoiceById(Long id);
    InvoiceModel saveOrUpdateInvoice(InvoiceDto invoiceDto);
    void deleteInvoice(Long id);
    void sendInvoiceEmail(String email);
    InvoiceDto getLatestInvoice();
}