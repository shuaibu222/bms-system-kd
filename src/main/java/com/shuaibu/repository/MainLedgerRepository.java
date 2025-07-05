package com.shuaibu.repository;

import com.shuaibu.model.MainLedgerModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MainLedgerRepository extends JpaRepository<MainLedgerModel, Long> {
    List<MainLedgerModel> findAllByDate(LocalDate date);

    // List<MainLedgerModel> findAllByDateBetween(LocalDate startDate, LocalDate
    // endDate);
    List<MainLedgerModel> findAllByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);

    Optional<MainLedgerModel> findTopByOrderByDateDescIdDesc(); // To get last closing balance

    boolean existsByDateAndParticulars(LocalDate date, String particulars);

    Optional<MainLedgerModel> findByDateAndParticulars(LocalDate date, String particulars);

}
