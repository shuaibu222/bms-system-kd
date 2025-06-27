package com.shuaibu.service;

import com.shuaibu.dto.SuspenseDto;
import java.util.List;

public interface SuspenseService {
    void saveOrUpdateSuspense(SuspenseDto suspenseDto);
    void deleteSuspense(Long id);
    SuspenseDto getSuspenseById(Long id);
    List<SuspenseDto> getAllSuspenses();
}
