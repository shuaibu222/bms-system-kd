package com.shuaibu.repository;

import com.shuaibu.model.CustomerModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerModel, Long> {
    Optional<CustomerModel> findByPhone(String phone);

    List<CustomerModel> findByNameContainingIgnoreCaseOrPhoneContaining(String name, String phone);
}