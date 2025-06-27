package com.shuaibu.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shuaibu.model.SaleModel;

@Repository
public interface SaleRepository extends JpaRepository<SaleModel, Long> {

    List<SaleModel> findBySaleDateTimeBetween(LocalDate startDate, LocalDate endDate);

    Optional<SaleModel> findTopByOrderByIdDesc();

    SaleModel findByQtnNum(String qtnNum);

}