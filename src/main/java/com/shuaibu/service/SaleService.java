package com.shuaibu.service;

import java.time.LocalDate;
import java.util.List;

import com.shuaibu.dto.SaleDto;
import com.shuaibu.model.SaleModel;

public interface SaleService {
    List<SaleDto> getAllSales();

    SaleDto getSaleById(Long id);

    void saveOrUpdateSale(SaleDto saleDto);

    void deleteSale(Long id);

    List<SaleModel> getSalesByDateRange(LocalDate startDate, LocalDate endDate);

    SaleDto getLatestSale();
}