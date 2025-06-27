package com.shuaibu.service.impl;

import com.shuaibu.dto.SuspenseDto;
import com.shuaibu.mapper.SuspenseMapper;
import com.shuaibu.repository.SuspenseRepository;
import com.shuaibu.service.SuspenseService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuspenseServiceImpl implements SuspenseService {

    private final SuspenseRepository suspenseRepository;

    public SuspenseServiceImpl(SuspenseRepository suspenseRepository) {
        this.suspenseRepository = suspenseRepository;
    }

    @Override
    public void saveOrUpdateSuspense(SuspenseDto suspenseDto) {
        suspenseRepository.save(SuspenseMapper.mapToModel(suspenseDto));
    }

    @Override
    public void deleteSuspense(Long id) {
        suspenseRepository.deleteById(id);
    }

    @Override
    public SuspenseDto getSuspenseById(Long id) {
        return SuspenseMapper.mapToDto(suspenseRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Suspense not found")));
    }

    @Override
    public List<SuspenseDto> getAllSuspenses() {
        return suspenseRepository.findAll().stream().map(SuspenseMapper::mapToDto).collect(Collectors.toList());
    }
}
