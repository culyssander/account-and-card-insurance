package com.santander.msauthservices.repository;

import com.santander.msauthservices.model.Insured;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface InsuredRepository extends JpaRepository<Insured, BigInteger> {

    boolean existsByCpf(String cpf);

    Optional<Insured> findByCpf(String cpf);
}
