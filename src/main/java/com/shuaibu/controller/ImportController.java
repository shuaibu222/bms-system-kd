package com.shuaibu.controller;

import com.shuaibu.dto.CustomerDto;
import com.shuaibu.dto.ProductDto;
import com.shuaibu.service.CustomerService;
import com.shuaibu.service.ExcelImportService;
import com.shuaibu.service.ProductService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class ImportController {

    private final ExcelImportService excelImportService;
    private final ProductService productService;
    private final CustomerService customerService;

    public ImportController(ExcelImportService excelImportService, ProductService productService,
                            CustomerService customerService) {
        this.excelImportService = excelImportService;
        this.productService = productService;
        this.customerService = customerService;
    }

    @PostMapping("/upload/products")
    public String importProductExcel(@RequestParam("file") MultipartFile file, Model model) {
        try {
            // Check file extension
            if (!isExcelFile(file)) {
                throw new IOException("Invalid file type. Please upload an Excel file (.xls or .xlsx).");
            }

            // Log basic info
            logFileInfo(file);

            // Import and save products
            List<ProductDto> products = excelImportService.importProductsFromExcel(file);
            productService.saveAll(products);

            model.addAttribute("message", "Successfully imported " + products.size() + " products");
            model.addAttribute("products", products);
            return "import-result";

        } catch (IOException e) {
            model.addAttribute("message", "Error importing file: " + e.getMessage());
            return "products/list";
        }
    }

    @PostMapping("/upload/customers")
    public String importCustomerExcel(@RequestParam("file") MultipartFile file, Model model) {
        try {
            // Check file extension
            if (!isExcelFile(file)) {
                throw new IOException("Invalid file type. Please upload an Excel file (.xls or .xlsx).");
            }

            // Log basic info
            logFileInfo(file);

            // Import and save customers
            List<CustomerDto> customers = excelImportService.importCustomersFromExcel(file);
            System.out.println(customers);
            customerService.saveAll(customers);

            model.addAttribute("message", "Successfully imported " + customers.size() + " customers");
            model.addAttribute("customers", customers);
            return "import-result";

        } catch (IOException e) {
            model.addAttribute("message", "Error importing file: " + e.getMessage());
            return "customers/list";
        }
    }

    private boolean isExcelFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename != null && (filename.endsWith(".xlsx") || filename.endsWith(".xls"));
    }

    private void logFileInfo(MultipartFile file) {
        System.out.println("File Content Type: " + file.getContentType());
        System.out.println("Original Filename: " + file.getOriginalFilename());
    }
}
