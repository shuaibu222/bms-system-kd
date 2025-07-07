package com.shuaibu.service;

import com.shuaibu.dto.CustomerDto;
import com.shuaibu.dto.CustomerStatementDto;
import com.shuaibu.dto.DepositDto;
import com.shuaibu.dto.InvoiceDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomerService {
    List<CustomerDto> getAllCustomers();

    InvoiceDto getInvoiceWithItems(Long invoiceId);

    CustomerDto getCustomerById(Long id);

    void saveOrUpdateCustomer(CustomerDto customerDto);

    void deleteCustomer(Long id);

    void saveAll(List<CustomerDto> customers);

    void makeDeposit(DepositDto depositDto);

    List<CustomerStatementDto> getStatementForCustomer(Long customerId);

    void addCustomerStatement(Long customerId, LocalDateTime date, String narration, double debit,
            double credit);

}