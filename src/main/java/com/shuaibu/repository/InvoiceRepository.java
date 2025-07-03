package com.shuaibu.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shuaibu.model.InvoiceModel;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceModel, Long> {

    Optional<InvoiceModel> findTopByOrderByIdDesc();

    List<InvoiceModel> findByInvoiceDateTimeBetween(LocalDate startDate, LocalDate endDate);

    List<InvoiceModel> findByCustomerId(Long id);

    List<InvoiceModel> findByInvoiceDateTime(LocalDate date);

    InvoiceModel findFirstByCustomerIdAndInvoiceDateTime(Long id, LocalDate date);

    InvoiceModel findByInvNum(String invNum);
}