package com.shuaibu.service;

import com.shuaibu.dto.CustomerDto;
import com.shuaibu.dto.ProductDto;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class ExcelImportService {

    // =================== PRODUCT IMPORT ===================
    public List<ProductDto> importProductsFromExcel(MultipartFile file) throws IOException {
        List<ProductDto> products = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        Map<String, Integer> headerMap = null;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            headerMap = tryMapHeaders(row, Arrays.asList("PRODUCT(S)", "CLOSING STOCK(QUANTITY)", "PRICE"));
            if (headerMap != null) break;
        }

        if (headerMap == null) {
            System.out.println("No valid header row found for products");
            workbook.close();
            return products;
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (isProductRowEmpty(row, headerMap)) continue;

            ProductDto product = ProductDto.builder()
                    .name(getStringValue(row, headerMap.get("PRODUCT(S)")))
                    .quantity(getIntValue(row, headerMap.get("CLOSING STOCK(QUANTITY)")))
                    .price(getNumericValue(row, headerMap.get("PRICE")))
                    .lowQuantityAlert(20)
                    .build();

            products.add(product);
        }

        workbook.close();
        return products;
    }

    // =================== CUSTOMER IMPORT ===================
    public List<CustomerDto> importCustomersFromExcel(MultipartFile file) throws IOException {
        List<CustomerDto> customers = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        Map<String, Integer> headerMap = null;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            headerMap = tryMapHeaders(row, Arrays.asList("CUSTOMERS NAME", "ADDRESS", "PHONE NUMBER", "BALANCE"));
            if (headerMap != null) break;
        }

        if (headerMap == null) {
            System.out.println("No valid header row found for customers");
            workbook.close();
            return customers;
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (isCustomerRowEmpty(row, headerMap)) continue;

            CustomerDto customer = CustomerDto.builder()
                    .name(getStringValue(row, headerMap.get("CUSTOMERS NAME")))
                    .address(getStringValue(row, headerMap.get("ADDRESS")))
                    .phone(getStringValue(row, headerMap.get("PHONE NUMBER")))
                    .balance(getNumericValue(row, headerMap.get("BALANCE")))
                    .build();

            System.out.println("Importing customer: " + customer.getName());
            customers.add(customer);
        }

        workbook.close();
        return customers;
    }

    // =================== HEADER MAPPING ===================
    private Map<String, Integer> tryMapHeaders(Row row, List<String> requiredHeaders) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (Cell cell : row) {
            String value = getStringValue(row, cell.getColumnIndex()).trim().toUpperCase(Locale.ROOT);
            if (!value.isEmpty()) {
                headerMap.put(value, cell.getColumnIndex());
            }
        }

        for (String required : requiredHeaders) {
            if (!headerMap.containsKey(required.toUpperCase(Locale.ROOT))) {
                return null;
            }
        }

        return headerMap;
    }

    // =================== ROW VALIDATORS ===================
    private boolean isProductRowEmpty(Row row, Map<String, Integer> headerMap) {
        String name = getStringValue(row, headerMap.getOrDefault("PRODUCT(S)", -1)).trim();
        double price = getNumericValue(row, headerMap.getOrDefault("PRICE", -1));
        int quantity = getIntValue(row, headerMap.getOrDefault("CLOSING STOCK(QUANTITY)", -1));

        if (name.equalsIgnoreCase("PRODUCT(S)") || name.toUpperCase().contains("PRODUCT")) return true;
        return name.isEmpty() && quantity == 0 && price == 0.0;
    }

    private boolean isCustomerRowEmpty(Row row, Map<String, Integer> headerMap) {
        String name = getStringValue(row, headerMap.getOrDefault("CUSTOMERS NAME", -1)).trim();
        String phone = getStringValue(row, headerMap.getOrDefault("PHONE NUMBER", -1)).trim();

        if (name.isEmpty() && phone.isEmpty()) return true;

        // Skip title/header/footer/total rows
        String upper = name.toUpperCase();
        return upper.contains("CUSTOMER") || upper.contains("BALANCE") || upper.contains("TOTAL") || upper.equals("SN");
    }

    // =================== CELL VALUE READERS ===================
    private String getStringValue(Row row, int cellNum) {
        if (cellNum < 0) return "";
        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private Double getNumericValue(Row row, int cellNum) {
        if (cellNum < 0) return 0.0;
        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        switch (cell.getCellType()) {
            case NUMERIC -> {
                return cell.getNumericCellValue();
            }
            case STRING -> {
                String raw = cell.getStringCellValue().replaceAll("[^\\d.\\-]", "").trim();
                try {
                    return Double.parseDouble(raw);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
            default -> {
                return 0.0;
            }
        }
    }

    private Integer getIntValue(Row row, int cellNum) {
        return getNumericValue(row, cellNum).intValue();
    }
}
