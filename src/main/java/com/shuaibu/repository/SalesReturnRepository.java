package com.shuaibu.repository;

import com.shuaibu.model.SalesReturnModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesReturnRepository extends JpaRepository<SalesReturnModel, Long> {}