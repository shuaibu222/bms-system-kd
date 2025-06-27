package com.shuaibu.service;

import com.shuaibu.dto.CustomerDto;
import java.util.List;

public interface CustomerService {
    List<CustomerDto> getAllCustomers();

    CustomerDto getCustomerById(Long id);

    void saveOrUpdateCustomer(CustomerDto customerDto);

    void deleteCustomer(Long id);

    void saveAll(List<CustomerDto> customers);
}