package com.shuaibu.service;

import com.shuaibu.dto.CustomerDto;
import com.shuaibu.dto.DepositDto;
import com.shuaibu.dto.InvoiceDto;

import java.util.List;

public interface CustomerService {
    List<CustomerDto> getAllCustomers();

    InvoiceDto getInvoiceWithItems(Long invoiceId);

    CustomerDto getCustomerById(Long id);

    void saveOrUpdateCustomer(CustomerDto customerDto);

    void deleteCustomer(Long id);

    void saveAll(List<CustomerDto> customers);

    void makeDeposit(DepositDto depositDto);

}