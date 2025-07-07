package com.shuaibu.controller;

import com.shuaibu.model.MainLedgerModel;
import com.shuaibu.repository.MainLedgerRepository;
import com.shuaibu.repository.MainLedgerSummaryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/main-ledger")
@RequiredArgsConstructor
public class MainLedgerController {

    private final MainLedgerRepository ledgerRepo;
    private final MainLedgerSummaryRepository summaryRepo;

    @GetMapping
    public String showLedgerPage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Model model) {

        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);

        if (start != null && end != null) {
            List<MainLedgerModel> entries = ledgerRepo.findAllByDateBetweenOrderByDateAsc(start, end);
            model.addAttribute("entries", entries);

            // Try to load static summary
            summaryRepo.findBySummaryDate(end).ifPresent(summary -> {
                model.addAttribute("totalExpenses", summary.getTotalExpenses());
                model.addAttribute("customerPayments", summary.getCustomerPayments());
                model.addAttribute("profitLoss", summary.getProfitOrLoss());
                model.addAttribute("finalLedgerBalance", summary.getFinalLedgerBalance());
                model.addAttribute("stockValue", summary.getStockValue());
                model.addAttribute("customerDebt", summary.getCustomerDebt());
                model.addAttribute("suspense", summary.getSuspense());
                model.addAttribute("staffLoans", summary.getStaffLoans());
            });
        }

        return "main-ledger/list";
    }
}
