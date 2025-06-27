package com.shuaibu.service.impl;

import com.shuaibu.dto.SalesReturnDto;
import com.shuaibu.mapper.SalesReturnMapper;
import com.shuaibu.repository.SalesReturnRepository;
import com.shuaibu.service.SalesReturnService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalesReturnImpl implements SalesReturnService {

    private final SalesReturnRepository salesReturnRepository;

    public SalesReturnImpl(SalesReturnRepository salesReturnRepository) {
        this.salesReturnRepository = salesReturnRepository;
    }

    @Override
    public List<SalesReturnDto> getAllSalesReturns() {
        return salesReturnRepository.findAll()
                .stream()
                .map(SalesReturnMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public SalesReturnDto getSalesReturnById(Long id) {
        return SalesReturnMapper.mapToDto(
                salesReturnRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Sales Return not found!"))
        );
    }

    @Override
    public void saveOrUpdateSalesReturn(SalesReturnDto salesReturnDto) {
        salesReturnRepository.save(SalesReturnMapper.mapToModel(salesReturnDto));
    }

    @Override
    public void deleteSalesReturn(Long id) {
        salesReturnRepository.deleteById(id);
    }
}