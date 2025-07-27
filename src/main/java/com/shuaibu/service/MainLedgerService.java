package com.shuaibu.service;

import com.shuaibu.model.InvoiceModel;
import com.shuaibu.model.MainLedgerModel;
import com.shuaibu.model.MainLedgerSummaryModel;
import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.repository.DepositRepository;
import com.shuaibu.repository.ExpenseRepository;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.repository.LoanRepository;
import com.shuaibu.repository.LowStockRepository;
import com.shuaibu.repository.MainLedgerRepository;
import com.shuaibu.repository.MainLedgerSummaryRepository;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.SuspenseRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MainLedgerService {

    private final MainLedgerRepository ledgerRepo;
    private final InvoiceRepository invoiceRepo;
    private final LowStockRepository lowStockRepo;
    private final ProductRepository productRepo;
    private final ExpenseRepository expenseRepos;
    private final DepositRepository depositRepo;
    private final SuspenseRepository suspenseRepo;
    private final LoanRepository loanRepo;
    private final CustomerRepository customerRepo;
    private final MainLedgerSummaryRepository summaryRepo;

    public void generateLedger(LocalDate startDate, LocalDate endDate) {
        // ✅ Check if any ledger entries already exist for the date range
        List<MainLedgerModel> existingEntries = ledgerRepo.findAllByDateBetweenOrderByDateAsc(startDate, endDate);
        if (!existingEntries.isEmpty()) {
            throw new IllegalStateException("Ledger entries already exist between " + startDate + " and " + endDate);
        }

        // ✅ Optional: Check if monthly summary exists for endDate
        if (summaryRepo.findBySummaryDate(endDate).isPresent()) {
            throw new IllegalStateException("Monthly summary already exists for " + endDate);
        }

        LocalDate prevDate = startDate.minusDays(1);
        List<MainLedgerModel> previousEntries = ledgerRepo.findAllByDateBetweenOrderByDateAsc(prevDate, prevDate);

        double rollingBalance;

        if (!previousEntries.isEmpty()) {
            // Use last closing balance from "End of Month Summary"
            rollingBalance = previousEntries.get(previousEntries.size() -
                    1).getClosingBalance();
        } else {
            // If nothing exists before, calculate initial balance as:
            // stock value + positive customer receivables + staff loans + suspense
            double stockValue = calculateStockValueBefore(startDate);
            double outstandingValues = calculateOutstandingValuesBefore(startDate);
            rollingBalance = stockValue + outstandingValues;
        }

        // STEP 2: Generate daily entries
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;

            double openingBalance = rollingBalance;

            // SALES ENTRIES (credit)
            double totalSales = 0;
            totalSales += saveSalesEntry(currentDate, "Cash", "CASH", openingBalance);
            totalSales += saveSalesEntry(currentDate, "Card", "CARD", openingBalance);
            totalSales += saveSalesEntry(currentDate, "Bank Transfer", "BANK TRANSFER", openingBalance);
            totalSales += saveSalesEntry(currentDate, "POS", "POS", openingBalance);

            rollingBalance -= totalSales;

            // PURCHASE ENTRY (debit)
            double totalPurchases = calculateDailyPurchases(currentDate);
            if (totalPurchases > 0) {
                ledgerRepo.save(MainLedgerModel.builder()
                        .date(currentDate)
                        .particulars("Purchase")
                        .credit(0.0)
                        .debit(totalPurchases)
                        .openingBalance(rollingBalance)
                        .closingBalance(rollingBalance + totalPurchases)
                        .build());

                rollingBalance += totalPurchases;
            }
        }
    }

    private double calculateOutstandingValuesBefore(LocalDate date) {
        // Customer with negative accounts (debts)
        double customerDebts = customerRepo.findAll().stream()
                .filter(c -> c.getBalance() != null && c.getBalance() < 0)
                .mapToDouble(c -> Math.abs(c
                        .getBalance()))
                .sum();

        // Staff loans total
        double staffLoans = loanRepo.findAll().stream()
                .filter(l -> !l.getDate().isAfter(date))
                .mapToDouble(l -> l.getAmount() - (l.getAmountRepaid() != null ? l.getAmountRepaid() : 0))
                .sum();

        // Suspense total
        double suspense = suspenseRepo.findAll().stream()
                .filter(s -> !s.getTransactionDate().isAfter(date))
                .filter(s -> "Pending".equalsIgnoreCase(s.getStatus()) || "Resolved".equalsIgnoreCase(s.getStatus()))
                .mapToDouble(s -> s.getAmount())
                .sum();

        return customerDebts + staffLoans + suspense;
    }

    private double saveSalesEntry(LocalDate date, String label, String paymentMethod, double openingBalance) {
        double salesAmount = invoiceRepo.findAll().stream()
                .filter(inv -> inv.getInvoiceDateTime().equals(date))
                .filter(inv -> paymentMethod.equalsIgnoreCase(inv.getPaymentMethod()))
                .mapToDouble(InvoiceModel::getInvoiceValue)
                .sum();

        if (salesAmount > 0) {
            double closing = openingBalance - salesAmount;
            ledgerRepo.save(MainLedgerModel.builder()
                    .date(date)
                    .particulars(label)
                    .credit(salesAmount)
                    .debit(0.0)
                    .openingBalance(openingBalance)
                    .closingBalance(closing)
                    .build());
            return salesAmount;
        }
        return 0.0;
    }

    private double calculateDailyPurchases(LocalDate date) {
        return lowStockRepo.findAll().stream()
                .filter(p -> p.getLowStockDate().equals(date))
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
    }

    private double calculateStockValueBefore(LocalDate date) {
        return productRepo.findAll().stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
    }

    public List<MainLedgerModel> getLedger(LocalDate start, LocalDate end) {
        return ledgerRepo.findAllByDateBetweenOrderByDateAsc(start, end);
    }

    public Map<String, Double> calculateLedgerSummary(LocalDate endDate) {
        double totalExpenses = expenseRepos.findAll().stream()
                .filter(e -> !e.getDate().isAfter(endDate))
                .mapToDouble(e -> e.getAmount().doubleValue())
                .sum();

        double totalCustomerPayments = depositRepo.findAll().stream()
                .filter(d -> !d.getDepositDate().isAfter(endDate))
                .mapToDouble(d -> d.getTotalAmount())
                .sum();

        double suspense = suspenseRepo.findAll().stream()
                .filter(s -> !s.getTransactionDate().isAfter(endDate))
                .filter(s -> "Pending".equalsIgnoreCase(s.getStatus()) || "Resolved".equalsIgnoreCase(s.getStatus()))
                .mapToDouble(s -> s.getAmount())
                .sum();

        double staffLoans = loanRepo.findAll().stream()
                .filter(l -> !l.getDate().isAfter(endDate))
                .mapToDouble(l -> l.getAmount()
                        - (l.getAmountRepaid() != null ? l.getAmountRepaid() : 0))
                .sum();

        // Customer debt (negative balance) and receivables (positive balance)
        double customerDebt = customerRepo.findAll().stream()
                .mapToDouble(c -> c.getBalance() != null && c.getBalance() < 0 ? Math.abs(c.getBalance()) : 0)
                .sum();

        // double customerReceivables = customerRepo.findAll().stream()
        // .mapToDouble(c -> c.getBalance() != null && c.getBalance() > 0 ?
        // c.getBalance() : 0)
        // .sum();

        double stockValue = productRepo.findAll().stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();

        // Get final closing balance
        List<MainLedgerModel> ledger = ledgerRepo.findAllByDateBetweenOrderByDateAsc(endDate.minusMonths(1),
                endDate);
        double finalLedgerBalance = ledger.isEmpty() ? 0.0
                : ledger.get(ledger.size()
                        - 1).getClosingBalance();

        double liabilities = stockValue + Math.abs(customerDebt) + suspense +
                staffLoans;
        // double assets = customerReceivables; // add other assets if needed
        double profitOrLoss = finalLedgerBalance - liabilities;

        Map<String, Double> summary = new HashMap<>();
        summary.put("totalExpenses", totalExpenses);
        summary.put("customerPayments", totalCustomerPayments);
        summary.put("profitLoss", profitOrLoss);
        summary.put("finalLedgerBalance", finalLedgerBalance);
        summary.put("stockValue", stockValue);
        summary.put("customerDebt", customerDebt);
        // summary.put("customerReceivables", customerReceivables);
        summary.put("suspense", suspense);
        summary.put("staffLoans", staffLoans);
        return summary;
    }

    public void saveEndOfMonthSummary(LocalDate monthEndDate) {
        if (summaryRepo.findBySummaryDate(monthEndDate).isPresent()) {
            throw new IllegalStateException("Monthly summary already exists for " + monthEndDate);
        }

        Map<String, Double> summary = calculateLedgerSummary(monthEndDate);

        double finalLedgerBalance = summary.getOrDefault("finalLedgerBalance", 0.0);
        double totalExpenses = summary.getOrDefault("totalExpenses", 0.0);
        double profitOrLoss = summary.getOrDefault("profitLoss", 0.0);
        double closingBalance = finalLedgerBalance - totalExpenses + profitOrLoss;

        // Save to main ledger
        MainLedgerModel summaryEntry = MainLedgerModel.builder()
                .date(monthEndDate)
                .particulars("End of Month Summary")
                .credit(profitOrLoss)
                .debit(totalExpenses)
                .openingBalance(finalLedgerBalance)
                .closingBalance(closingBalance)
                .build();
        ledgerRepo.save(summaryEntry);

        // Save to summary snapshot table
        MainLedgerSummaryModel snapshot = MainLedgerSummaryModel.builder()
                .summaryDate(monthEndDate)
                .totalExpenses(totalExpenses)
                .customerPayments(summary.getOrDefault("customerPayments", 0.0))
                .profitOrLoss(profitOrLoss)
                .finalLedgerBalance(finalLedgerBalance)
                .stockValue(summary.getOrDefault("stockValue", 0.0))
                .customerDebt(summary.getOrDefault("customerDebt", 0.0))
                .suspense(summary.getOrDefault("suspense", 0.0))
                .staffLoans(summary.getOrDefault("staffLoans", 0.0))
                .build();
        summaryRepo.save(snapshot);
    }

}