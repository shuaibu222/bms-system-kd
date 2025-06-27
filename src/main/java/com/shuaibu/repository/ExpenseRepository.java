package com.shuaibu.repository;

import com.shuaibu.model.ExpenseModel;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseModel, Long> {
    List<ExpenseModel> findByDateBetween(LocalDate startDate, LocalDate endDate);
}