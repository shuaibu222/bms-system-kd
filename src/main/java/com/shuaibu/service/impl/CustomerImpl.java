package com.shuaibu.service.impl;

import com.shuaibu.dto.CustomerDto;
import com.shuaibu.mapper.CustomerMapper;
import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.service.CustomerService;
import org.springframework.stereotype.Service;

import com.shuaibu.dto.DepositDto;
import com.shuaibu.mapper.DepositMapper;
import com.shuaibu.model.CustomerModel;
import com.shuaibu.model.DepositModel;
import com.shuaibu.repository.DepositRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final DepositRepository depositRepository;

    public CustomerImpl(CustomerRepository customerRepository, DepositRepository depositRepository) {
        this.customerRepository = customerRepository;
        this.depositRepository = depositRepository;
    }

    @Override
    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(CustomerMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void makeDeposit(DepositDto depositDto) {
        // Save deposit
        DepositModel deposit = DepositMapper.mapToModel(depositDto);
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveAll'");
    }
}