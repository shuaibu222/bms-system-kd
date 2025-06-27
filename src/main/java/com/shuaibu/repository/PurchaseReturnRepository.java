package com.shuaibu.repository;

import com.shuaibu.model.PurchaseReturnModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseReturnRepository extends JpaRepository<PurchaseReturnModel, Long> {}