package com.shuaibu.mapper;

import com.shuaibu.dto.InvoiceDto;
import com.shuaibu.model.InvoiceModel;

public class InvoiceMapper {

    public static InvoiceDto mapToDto(InvoiceModel invoiceModel) {
        return InvoiceDto.builder()
                .id(invoiceModel.getId())
                .invNum(invoiceModel.getInvNum())
                .quotationId(invoiceModel.getQuotationId())
                .totalAmount(invoiceModel.getTotalAmount())
                .invoiceValue(invoiceModel.getInvoiceValue())
                .balanceDue(invoiceModel.getBalanceDue())
                .paymentStatus(invoiceModel.getPaymentStatus())
                .paymentMethod(invoiceModel.getPaymentMethod())
                .invoiceDateTime(invoiceModel.getInvoiceDateTime())
                .build();
    }

    public static InvoiceModel mapToModel(InvoiceDto invoiceDto) {

        return InvoiceModel.builder()
                .id(invoiceDto.getId())
                .invNum(invoiceDto.getInvNum())
                .quotationId(invoiceDto.getQuotationId())
                .totalAmount(invoiceDto.getTotalAmount())
                .invoiceValue(invoiceDto.getInvoiceValue())
                .balanceDue(invoiceDto.getBalanceDue())
                .paymentStatus(invoiceDto.getPaymentStatus())
                .paymentMethod(invoiceDto.getPaymentMethod())
                .invoiceDateTime(invoiceDto.getInvoiceDateTime())
                .build();
    }
}
