package com.shuaibu.repository;

import com.shuaibu.model.CustomerModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerModel, Long> {
    List<CustomerModel> findByNameContainingIgnoreCaseOrPhoneContaining(String name, String phone);
}