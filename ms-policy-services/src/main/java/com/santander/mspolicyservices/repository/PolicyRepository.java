package com.santander.mspolicyservices.repository;

import com.santander.mspolicyservices.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, BigInteger> {

    Optional<Policy> findByPolicyNumber(String policyNumber);

    Optional<Policy> findByCpfAndPolicyNumber(String cpf, String policyNumber);
}
