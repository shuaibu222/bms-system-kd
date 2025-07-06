package com.shuaibu.service.impl;

import com.shuaibu.dto.PurchaseReturnDto;
import com.shuaibu.mapper.PurchaseReturnMapper;
import com.shuaibu.model.ProductModel;
import com.shuaibu.model.PurchaseReturnModel;
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
                        .orElseThrow(() -> new IllegalArgumentException("Purchase Return not found with ID: " + id)));
    }

    @Override
    public void saveOrUpdatePurchaseReturn(PurchaseReturnDto purchaseReturnDto) {
        if (purchaseReturnDto == null) {
            throw new IllegalArgumentException("Purchase return data cannot be null");
        }

        // 🧠 Fetch product by name
        ProductModel productModel = productRepository.findByName(purchaseReturnDto.getProductName());

        if (productModel == null) {
            throw new IllegalArgumentException("Product not found with name: " + purchaseReturnDto.getProductName());
        }

        // 🛑 Ensure quantity does not go negative
        int newQuantity = productModel.getQuantity() - purchaseReturnDto.getQuantity();
        if (newQuantity < 0) {
            throw new IllegalStateException("Cannot return more than available stock");
        }

        productModel.setQuantity(newQuantity);
        productRepository.save(productModel);

        // Save purchase return
        PurchaseReturnModel model = PurchaseReturnMapper.mapToModel(purchaseReturnDto);
        purchaseReturnRepository.save(model);
    }

    @Override
    public void deletePurchaseReturn(Long id) {
        if (!purchaseReturnRepository.existsById(id)) {
            throw new IllegalArgumentException("Purchase return not found with ID: " + id);
        }

        purchaseReturnRepository.deleteById(id);
    }
}
