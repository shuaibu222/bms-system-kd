package com.shuaibu.repository;

import com.shuaibu.model.MainLedgerModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MainLedgerRepository extends JpaRepository<MainLedgerModel, Long> {

    List<MainLedgerModel> findAllByDate(LocalDate date);

    List<MainLedgerModel> findAllByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);

    Optional<MainLedgerModel> findTopByOrderByDateDescIdDesc();

    boolean existsByDateAndParticulars(LocalDate date, String particulars);

    Optional<MainLedgerModel> findByDateAndParticulars(LocalDate date, String particulars);

    @Query("SELECT DISTINCT FUNCTION('YEAR', m.date), FUNCTION('MONTH', m.date) " +
            "FROM MainLedgerModel m ORDER BY 1 DESC, 2 DESC")
    List<Object[]> findDistinctYearMonthParts();
}
