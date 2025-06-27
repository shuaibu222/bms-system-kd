package com.shuaibu.service.impl;

import com.shuaibu.dto.CustomerDto;
import com.shuaibu.mapper.CustomerMapper;
import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.service.CustomerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(CustomerMapper::mapToDto)
                .collect(Collectors.toList());
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