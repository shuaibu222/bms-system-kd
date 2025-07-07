package com.shuaibu.repository;

import com.shuaibu.model.MainLedgerSummaryModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MainLedgerSummaryRepository extends JpaRepository<MainLedgerSummaryModel, Long> {
    Optional<MainLedgerSummaryModel> findBySummaryDate(LocalDate summaryDate);
}
