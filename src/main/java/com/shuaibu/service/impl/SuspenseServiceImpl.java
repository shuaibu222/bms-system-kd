package com.shuaibu.service.impl;

import com.shuaibu.dto.SuspenseDto;
import com.shuaibu.mapper.SuspenseMapper;
import com.shuaibu.model.SuspenseModel;
import com.shuaibu.repository.SuspenseRepository;
import com.shuaibu.service.SuspenseService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuspenseServiceImpl implements SuspenseService {

    private final SuspenseRepository suspenseRepository;

    @Override
    public void saveOrUpdateSuspense(SuspenseDto suspenseDto) {
        if (suspenseDto == null) {
            throw new IllegalArgumentException("Suspense data must not be null");
        }

        // Optional: Validate amount
        if (suspenseDto.getAmount() == null || suspenseDto.getAmount() <= 0) {
            throw new IllegalArgumentException("Suspense amount must be a positive number");
        }

        suspenseRepository.save(SuspenseMapper.mapToModel(suspenseDto));
    }

    @Override
    public void deleteSuspense(Long id) {
        if (!suspenseRepository.existsById(id)) {
            throw new IllegalArgumentException("Suspense entry not found with ID: " + id);
        }
        suspenseRepository.deleteById(id);
    }

    @Override
    public SuspenseDto getSuspenseById(Long id) {
        SuspenseModel suspense = suspenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Suspense entry not found with ID: " + id));
        return SuspenseMapper.mapToDto(suspense);
    }

    @Override
    public List<SuspenseDto> getAllSuspenses() {
        return suspenseRepository.findAll()
                .stream()
                .map(SuspenseMapper::mapToDto)
                .collect(Collectors.toList());
    }
}
