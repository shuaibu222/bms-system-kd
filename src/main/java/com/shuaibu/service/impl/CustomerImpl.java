package com.shuaibu.service.impl;

import com.shuaibu.dto.*;
import com.shuaibu.mapper.CustomerMapper;
import com.shuaibu.mapper.DepositMapper;
import com.shuaibu.mapper.SaleMapper;
import com.shuaibu.model.*;
import com.shuaibu.repository.*;
import com.shuaibu.service.CustomerService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final DepositRepository depositRepository;
    private final InvoiceRepository invoiceRepository;
    private final SaleRepository saleRepository;

    @Override
    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(CustomerMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDto getCustomerById(Long id) {
        CustomerModel customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found!"));
        return CustomerMapper.mapToDto(customer);
    }

    @Override
    public void saveOrUpdateCustomer(CustomerDto customerDto) {
        CustomerModel model = CustomerMapper.mapToModel(customerDto);
        customerRepository.save(model);
    }

    @Override
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    @Override
    public void saveAll(List<CustomerDto> customers) {
        if (customers == null || customers.isEmpty()) {
            throw new IllegalArgumentException("Customer list must not be null or empty");
        }

        List<CustomerModel> models = customers.stream()
                .map(CustomerMapper::mapToModel)
                .collect(Collectors.toList());

        customerRepository.saveAll(models);
    }

    @Override
    public InvoiceDto getInvoiceWithItems(Long invoiceId) {
        InvoiceModel invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        SaleDto saleDto = null;
        if (invoice.getQuotationId() != null) {
            SaleModel sale = saleRepository.findById(invoice.getQuotationId()).orElse(null);
            if (sale != null) {
                saleDto = SaleDto.builder()
                        .id(sale.getId())
                        .qtnNum(sale.getQtnNum())
                        .customerName(sale.getCustomerName())
                        .phone(sale.getPhone())
                        .totalAmount(sale.getTotalAmount())
                        .saleDateTime(sale.getSaleDateTime())
                        .items(sale.getItems().stream()
                                .map(SaleMapper::mapItemToDto)
                                .collect(Collectors.toList()))
                        .build();
            }
        }

        return InvoiceDto.builder()
                .id(invoice.getId())
                .invNum(invoice.getInvNum())
                .quotationId(invoice.getQuotationId())
                .customerId(invoice.getCustomerId())
                .balanceDue(invoice.getBalanceDue())
                .totalAmount(invoice.getTotalAmount())
                .invoiceValue(invoice.getInvoiceValue())
                .paymentStatus(invoice.getPaymentStatus())
                .paymentMethod(invoice.getPaymentMethod())
                .invoiceDateTime(invoice.getInvoiceDateTime())
                .saleDto(saleDto)
                .build();
    }

    @Override
    public void makeDeposit(DepositDto depositDto) {
        DepositModel deposit = DepositMapper.mapToModel(depositDto);
        deposit.setId(null); // Ensure it's saved as new

        depositRepository.save(deposit);

        // Update customer balance
        CustomerModel customer = customerRepository.findById(depositDto.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer not found with ID: " + depositDto.getCustomerId()));

        double currentBalance = customer.getBalance() != null ? customer.getBalance() : 0.0;
        customer.setBalance(currentBalance + depositDto.getTotalAmount());

        customerRepository.save(customer);
    }
}
