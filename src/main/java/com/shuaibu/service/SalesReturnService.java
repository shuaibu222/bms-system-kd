package com.shuaibu.service;

import com.shuaibu.dto.SalesReturnDto;
import java.util.List;

public interface SalesReturnService {
    List<SalesReturnDto> getAllSalesReturns();
    SalesReturnDto getSalesReturnById(Long id);
    void saveOrUpdateSalesReturn(SalesReturnDto salesReturnDto);
    void deleteSalesReturn(Long id);
}