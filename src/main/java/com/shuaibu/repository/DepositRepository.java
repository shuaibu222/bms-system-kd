package com.shuaibu.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shuaibu.model.DepositModel;

@Repository
public interface DepositRepository extends JpaRepository<DepositModel, Long> {

    List<DepositModel> findByCustomerIdAndDepositDateBetween(Long id, LocalDate start, LocalDate end);

    List<DepositModel> findByDepositDateBetween(LocalDate start, LocalDate end);}