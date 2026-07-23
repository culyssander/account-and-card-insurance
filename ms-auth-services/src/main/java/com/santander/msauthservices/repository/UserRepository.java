package com.santander.msauthservices.repository;

import com.santander.msauthservices.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, BigInteger> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
