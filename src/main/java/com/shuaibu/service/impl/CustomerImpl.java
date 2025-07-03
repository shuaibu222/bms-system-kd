package com.shuaibu.service.impl;

import com.shuaibu.dto.CustomerDto;
import com.shuaibu.mapper.CustomerMapper;
import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.service.CustomerService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.shuaibu.dto.DepositDto;
import com.shuaibu.dto.InvoiceDto;
import com.shuaibu.dto.SaleDto;
import com.shuaibu.mapper.DepositMapper;
import com.shuaibu.mapper.SaleMapper;
import com.shuaibu.model.CustomerModel;
import com.shuaibu.model.DepositModel;
import com.shuaibu.model.InvoiceModel;
import com.shuaibu.model.SaleModel;
import com.shuaibu.repository.DepositRepository;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.repository.SaleRepository;

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
        return customerRepository.findAll()
                .stream()
                .map(CustomerMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceDto getInvoiceWithItems(Long invoiceId) {
        InvoiceModel inv = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        SaleDto saleDto = null;
        if (inv.getQuotationId() != null) {
            SaleModel sale = saleRepository.findById(inv.getQuotationId())
                    .orElse(null);

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
                .id(inv.getId())
                .invNum(inv.getInvNum())
                .quotationId(inv.getQuotationId())
                .customerId(inv.getCustomerId())
                .balanceDue(inv.getBalanceDue())
                .totalAmount(inv.getTotalAmount())
                .invoiceValue(inv.getInvoiceValue())
                .paymentStatus(inv.getPaymentStatus())
                .paymentMethod(inv.getPaymentMethod())
                .invoiceDateTime(inv.getInvoiceDateTime())
                .saleDto(saleDto)
                .build();
    }

    @Override
    public void makeDeposit(DepositDto depositDto) {
        // Save deposit
        DepositModel deposit = DepositMapper.mapToModel(depositDto);
        deposit.setId(null);
        depositRepository.save(deposit);

        // Update customer balance
        CustomerModel customer = customerRepository.findById(depositDto.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer not found with ID: " + depositDto.getCustomerId()));

        double currentBalance = customer.getBalance() != null ? customer.getBalance() : 0.0;
        customer.setBalance(currentBalance + depositDto.getTotalAmount());

        customerRepository.save(customer);
    }

    @Override
    public CustomerDto getCustomerById(Long id) {
        return CustomerMapper.mapToDto(
                customerRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Customer not found!")));
    }

    @Override
    public void saveOrUpdateCustomer(CustomerDto customerDto) {
        customerRepository.save(CustomerMapper.mapToModel(customerDto));
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

        List<CustomerModel> customerModels = customers.stream()
                .map(CustomerMapper::mapToModel)
                .collect(Collectors.toList());

        customerRepository.saveAll(customerModels);
    }

}