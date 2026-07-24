package com.santander.mspolicyservices.repository;

import com.santander.mspolicyservices.model.ProtocolSequencial;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProtocolSequencialRepository extends JpaRepository<ProtocolSequencial, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProtocolSequencial p WHERE p.productCode = :departamento AND p.year = :ano")
    Optional<ProtocolSequencial> findProtocolByProductCodeAndYear(
            @Param("productCode") String departamento,
            @Param("year") Integer ano);
}