package com.shuaibu.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shuaibu.model.SuspenseModel;

@Repository
public interface SuspenseRepository extends JpaRepository<SuspenseModel, Long> {
    List<SuspenseModel> findByTransactionDateBetween(LocalDate start, LocalDate end);
}