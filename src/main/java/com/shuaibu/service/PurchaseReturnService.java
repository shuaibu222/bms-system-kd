package com.shuaibu.service;

import com.shuaibu.dto.PurchaseReturnDto;
import java.util.List;

public interface PurchaseReturnService {
    List<PurchaseReturnDto> getAllPurchaseReturns();
    PurchaseReturnDto getPurchaseReturnById(Long id);
    void saveOrUpdatePurchaseReturn(PurchaseReturnDto purchaseReturnDto);
    void deletePurchaseReturn(Long id);
}