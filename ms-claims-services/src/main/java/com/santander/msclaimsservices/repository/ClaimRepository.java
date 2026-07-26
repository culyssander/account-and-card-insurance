package com.santander.msclaimsservices.repository;

import com.santander.msclaimsservices.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, BigInteger> {
    Optional<Claim> findByClaimNumber(String claimNumber);
}
