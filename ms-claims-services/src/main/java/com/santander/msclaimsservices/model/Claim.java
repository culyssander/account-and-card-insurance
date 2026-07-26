package com.santander.msclaimsservices.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "claim_id_seq")
    private BigInteger id;

    @Column(name = "policy_id")
    private BigInteger policyId;

    @Column(name = "claim_number")
    private String claimNumber;

    private String status;
    private String description;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "opening_date")
    private LocalDateTime openingDate;

    @Column(name = "claimed_amount")
    private BigDecimal claimedAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
