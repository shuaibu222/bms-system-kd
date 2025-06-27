package com.shuaibu.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shuaibu.model.LowStockModel;

@Repository
public interface LowStockRepository extends JpaRepository<LowStockModel, Long> {

    List<LowStockModel> findByLowStockDateBetween(LocalDate start, LocalDate end);

    Optional<LowStockModel> findByName(String name);}