package com.shuaibu.repository;

import com.shuaibu.model.MainLedgerSummaryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface MainLedgerSummaryRepository extends JpaRepository<MainLedgerSummaryModel, Long> {
    Optional<MainLedgerSummaryModel> findBySummaryDate(LocalDate summaryDate);

    @Query("SELECT s FROM MainLedgerSummaryModel s " +
            "WHERE FUNCTION('YEAR', s.summaryDate) = :year " +
            "AND FUNCTION('MONTH', s.summaryDate) = :month")
    Optional<MainLedgerSummaryModel> findByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
