package com.shuaibu.service;

import com.shuaibu.model.InvoiceModel;
import com.shuaibu.model.MainLedgerModel;
import com.shuaibu.repository.CustomerRepository;
import com.shuaibu.repository.DepositRepository;
import com.shuaibu.repository.ExpenseRepository;
import com.shuaibu.repository.InvoiceRepository;
import com.shuaibu.repository.LoanRepository;
import com.shuaibu.repository.LowStockRepository;
import com.shuaibu.repository.MainLedgerRepository;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.SuspenseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class MainLedgerService {

    @Autowired
    private MainLedgerRepository ledgerRepo;
    @Autowired
    private InvoiceRepository invoiceRepo;
    @Autowired
    private LowStockRepository lowStockRepo;
    @Autowired
    private ProductRepository productRepo;
    @Autowired
    private ExpenseRepository expenseRepos;
    @Autowired
    private DepositRepository depositRepo;
    @Autowired
    private SuspenseRepository suspenseRepo;
    @Autowired
    private LoanRepository loanRepo;
    @Autowired
    private CustomerRepository customerRepo;

    public void generateLedger(LocalDate startDate, LocalDate endDate) {

        LocalDate prevDate = startDate.minusDays(1);
        List<MainLedgerModel> previousEntries = ledgerRepo.findAllByDateBetweenOrderByDateAsc(prevDate, prevDate);

        double rollingBalance;

        if (!previousEntries.isEmpty()) {
            // Use last closing balance from "End of Month Summary"
            rollingBalance = previousEntries.get(previousEntries.size() -
                    1).getClosingBalance();
        } else {
            // If nothing exists before, calculate initial balance as:
            // stock value + customer receivables + staff loans + suspense
            double stockValue = calculateStockValueBefore(startDate);
            double outstandingValues = calculateOutstandingValuesBefore(startDate);
            rollingBalance = stockValue + outstandingValues;
        }

        // STEP 2: Generate daily entries
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;

            double openingBalance = rollingBalance;

            Predicate<InvoiceModel> isPaidOrPartial = inv -> ("PAID".equalsIgnoreCase(inv.getPaymentStatus()) ||
                    "PARTIAL".equalsIgnoreCase(inv.getPaymentStatus())) &&
                    inv.getInvoiceDateTime().equals(currentDate);

            // SALES ENTRIES (credit)
            double totalSales = 0;
            totalSales += saveSalesEntry(currentDate, "Cash", isPaidOrPartial, "CASH",
                    openingBalance);
            totalSales += saveSalesEntry(currentDate, "Card", isPaidOrPartial, "CARD",
                    openingBalance);
            totalSales += saveSalesEntry(currentDate, "Bank Transfer", isPaidOrPartial,
                    "BANK TRANSFER",
                    openingBalance);
            totalSales += saveSalesEntry(currentDate, "POS", isPaidOrPartial, "POS",
                    openingBalance);

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
        // Customer with positive accounts (receivables)
        double customerReceivables = customerRepo.findAll().stream()
                .filter(c -> c.getBalance() != null && c.getBalance() > 0)
                .mapToDouble(c -> c.getBalance())
                .sum();

        // Staff loans total
        double staffLoans = loanRepo.findAll().stream()
                .filter(l -> !l.getLoanDate().isAfter(date))
                .mapToDouble(l -> l.getAmount() - (l.getAmountRepaid() != null ? l.getAmountRepaid() : 0))
                .sum();

        // Suspense total
        double suspense = suspenseRepo.findAll().stream()
                .filter(s -> !s.getTransactionDate().isAfter(date))
                .mapToDouble(s -> s.getAmount())
                .sum();

        return customerReceivables + staffLoans + suspense;
    }

    private double saveSalesEntry(LocalDate date, String label,
            Predicate<InvoiceModel> baseFilter,
            String paymentMethod, double openingBalance) {
        double salesAmount = invoiceRepo.findAll().stream()
                .filter(baseFilter)
                .filter(inv -> paymentMethod.equalsIgnoreCase(inv.getPaymentMethod()))
                .mapToDouble(InvoiceModel::getTotalAmount)
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
                .mapToDouble(s -> s.getAmount())
                .sum();

        double staffLoans = loanRepo.findAll().stream()
                .filter(l -> !l.getLoanDate().isAfter(endDate))
                .mapToDouble(l -> l.getAmount()
                        - (l.getAmountRepaid() != null ? l.getAmountRepaid() : 0))
                .sum();

        // Customer debt (negative balance) and receivables (positive balance)
        double customerDebt = customerRepo.findAll().stream()
                .mapToDouble(c -> c.getBalance() != null && c.getBalance() < 0 ? c.getBalance() : 0)
                .sum();

        double customerReceivables = customerRepo.findAll().stream()
                .mapToDouble(c -> c.getBalance() != null && c.getBalance() > 0 ? c.getBalance() : 0)
                .sum();

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
        double assets = customerReceivables; // add other assets if needed
        double profitOrLoss = finalLedgerBalance - (liabilities + assets);

        Map<String, Double> summary = new HashMap<>();
        summary.put("totalExpenses", totalExpenses);
        summary.put("customerPayments", totalCustomerPayments);
        summary.put("profitLoss", profitOrLoss);
        summary.put("finalLedgerBalance", finalLedgerBalance);
        summary.put("stockValue", stockValue);
        summary.put("customerDebt", Math.abs(customerDebt));
        summary.put("customerReceivables", customerReceivables);
        summary.put("suspense", suspense);
        summary.put("staffLoans", staffLoans);
        return summary;
    }

    public void saveEndOfMonthSummary(LocalDate monthEndDate) {
        // Get summary data
        Map<String, Double> summary = calculateLedgerSummary(monthEndDate);

        double finalLedgerBalance = summary.getOrDefault("finalLedgerBalance", 0.0);
        double totalExpenses = summary.getOrDefault("totalExpenses", 0.0);
        double profitOrLoss = summary.getOrDefault("profitLoss", 0.0);

        double closingBalance = finalLedgerBalance - totalExpenses + profitOrLoss;

        MainLedgerModel summaryEntry = MainLedgerModel.builder()
                .date(monthEndDate)
                .particulars("End of Month Summary")
                .credit(profitOrLoss)
                .debit(totalExpenses)
                .openingBalance(finalLedgerBalance)
                .closingBalance(closingBalance)
                .build();

        ledgerRepo.save(summaryEntry);
    }
}