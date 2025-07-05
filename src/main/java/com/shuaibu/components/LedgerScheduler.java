package com.shuaibu.components;

import com.shuaibu.service.MainLedgerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class LedgerScheduler {

    private final MainLedgerService mainLedgerService;

    public LedgerScheduler(MainLedgerService mainLedgerService) {
        this.mainLedgerService = mainLedgerService;
    }

    /**
     * Scheduled to run on the last day of every month at 1:00 AM.
     * It generates the full ledger for the month and saves the month-end summary.
     */
    @Scheduled(cron = "0 0 1 L * ?") // last day of every month at 1:00 AM
    public void generateMonthlyLedgerSummary() {
        LocalDate today = LocalDate.now(); // e.g. July 31
        LocalDate lastMonth = today.minusMonths(1); // e.g. June
        LocalDate startOfLastMonth = lastMonth.withDayOfMonth(1); // June 1
        LocalDate endOfLastMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()); // June 30

        // Step 1: Generate daily ledger for the full last month
        mainLedgerService.generateLedger(startOfLastMonth, endOfLastMonth);

        // Step 2: Save End-of-Month Summary (will skip if it already exists)
        mainLedgerService.saveEndOfMonthSummary(endOfLastMonth);
    }
}
