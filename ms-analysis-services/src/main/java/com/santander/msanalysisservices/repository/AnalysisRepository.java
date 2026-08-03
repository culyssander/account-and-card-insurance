package com.santander.msanalysisservices.repository;

import com.santander.msanalysisservices.model.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, BigInteger> {
    Optional<Analysis> findByClaimId(String claimId);

    boolean existsByClaimId(String claimId);
}
