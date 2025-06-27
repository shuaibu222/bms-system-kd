package com.shuaibu.repository;

import com.shuaibu.model.StaffModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<StaffModel, Long> {
    List<StaffModel> findByNameContainingIgnoreCaseOrPhoneContaining(String name, String phone);

    StaffModel findByPhone(String staffPhone);
}