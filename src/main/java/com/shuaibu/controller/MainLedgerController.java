package com.shuaibu.controller;

import com.shuaibu.model.MainLedgerModel;
import com.shuaibu.model.MainLedgerSummaryModel;
import com.shuaibu.repository.MainLedgerRepository;
import com.shuaibu.repository.MainLedgerSummaryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class MainLedgerController {

    @Autowired
    private MainLedgerRepository ledgerRepo;

    @Autowired
    private MainLedgerSummaryRepository summaryRepo;

    @GetMapping("/main-ledger")
    public String showMainLedger(@RequestParam(required = false) String month, Model model) {
        List<Object[]> rawYearMonthParts = ledgerRepo.findDistinctYearMonthParts();

        Map<String, String> monthOptions = rawYearMonthParts.stream().collect(Collectors.toMap(
                ym -> String.format("%04d-%02d", (int) ym[0], (int) ym[1]),
                ym -> YearMonth.of((int) ym[0], (int) ym[1])
                        .format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                (a, b) -> a,
                LinkedHashMap::new));

        if (month == null && !monthOptions.isEmpty()) {
            month = monthOptions.keySet().iterator().next(); // Use latest
        }

        model.addAttribute("monthOptions", monthOptions);
        model.addAttribute("selectedMonth", month);

        if (month != null) {
            String[] parts = month.split("-");
            int year = Integer.parseInt(parts[0]);
            int mon = Integer.parseInt(parts[1]);
            YearMonth ym = YearMonth.of(year, mon);

            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            // Fetch entries
            List<MainLedgerModel> entries = ledgerRepo.findAllByDateBetweenOrderByDateAsc(start, end);
            model.addAttribute("entries", entries);
            model.addAttribute("startDate", start);
            model.addAttribute("endDate", end);

            // Fetch summary
            Optional<MainLedgerSummaryModel> optionalSummary = summaryRepo.findByYearAndMonth(year, mon);
            optionalSummary.ifPresent(summary -> {
                model.addAttribute("totalExpenses", summary.getTotalExpenses());
                model.addAttribute("stockValue", summary.getStockValue());
                model.addAttribute("customerDebt", summary.getCustomerDebt());
                model.addAttribute("suspense", summary.getSuspense());
                model.addAttribute("staffLoans", summary.getStaffLoans());
                model.addAttribute("finalLedgerBalance", summary.getFinalLedgerBalance());
                model.addAttribute("profitLoss", summary.getProfitOrLoss());
            });
        } else {
            model.addAttribute("entries", new ArrayList<>());
        }

        return "main-ledger/list";
    }
}
