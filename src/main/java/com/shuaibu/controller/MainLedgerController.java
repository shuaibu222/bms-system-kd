package com.shuaibu.controller;

import com.shuaibu.model.MainLedgerModel;
import com.shuaibu.repository.MainLedgerRepository;
import com.shuaibu.service.MainLedgerService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/main-ledger")
@RequiredArgsConstructor
public class MainLedgerController {

    private final MainLedgerService ledgerService;
    private final MainLedgerRepository ledgerRepo;

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

            Map<String, Double> summary = ledgerService.calculateLedgerSummary(end);
            model.addAllAttributes(summary);
        }

        return "main-ledger/list";
    }
}
