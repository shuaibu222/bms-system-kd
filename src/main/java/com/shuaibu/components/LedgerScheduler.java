package com.shuaibu.components;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.shuaibu.service.MainLedgerService;

import java.time.LocalDate;

@Component
public class LedgerScheduler {

    private final MainLedgerService mainLedgerService;

    public LedgerScheduler(MainLedgerService mainLedgerService) {
        this.mainLedgerService = mainLedgerService;
    }

    @Scheduled(cron = "0 3 21 30 6 ?")
    // @Scheduled(cron = "0 0 1 L * ?") // Runs on the last day of every month at
    // 1:00 AM
    public void generateMonthlyLedgerSummary() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        LocalDate endOfLastMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
        mainLedgerService.saveEndOfMonthSummary(endOfLastMonth);
    }
}
