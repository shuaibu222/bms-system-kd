package com.shuaibu.repository;

import com.shuaibu.model.CustomerStatementModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerStatementRepository extends JpaRepository<CustomerStatementModel, Long> {
    List<CustomerStatementModel> findByCustomerIdOrderByTransactionDateAsc(Long customerId);
}