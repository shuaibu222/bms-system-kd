package com.shuaibu.service.impl;

import com.shuaibu.dto.PurchaseReturnDto;
import com.shuaibu.mapper.PurchaseReturnMapper;
import com.shuaibu.model.ProductModel;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.PurchaseReturnRepository;
import com.shuaibu.service.PurchaseReturnService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseReturnImpl implements PurchaseReturnService {

    private final PurchaseReturnRepository purchaseReturnRepository;
    private final ProductRepository productRepository;

    public PurchaseReturnImpl(PurchaseReturnRepository purchaseReturnRepository,
            ProductRepository productRepository) {
        this.purchaseReturnRepository = purchaseReturnRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<PurchaseReturnDto> getAllPurchaseReturns() {
        return purchaseReturnRepository.findAll()
                .stream()
                .map(PurchaseReturnMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PurchaseReturnDto getPurchaseReturnById(Long id) {
        return PurchaseReturnMapper.mapToDto(
                purchaseReturnRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Purchase Return not found!")));
    }

    @Override
    public void saveOrUpdatePurchaseReturn(PurchaseReturnDto purchaseReturnDto) {
        // Find the product by name and store
        ProductModel productModel = productRepository.findByName(purchaseReturnDto.getProductName());

        if (productModel != null) {
            productModel.setQuantity(productModel.getQuantity() - purchaseReturnDto.getQuantity());
            productRepository.save(productModel);
        }

        purchaseReturnRepository.save(PurchaseReturnMapper.mapToModel(purchaseReturnDto));
    }

    @Override
    public void deletePurchaseReturn(Long id) {
        purchaseReturnRepository.deleteById(id);
    }
}