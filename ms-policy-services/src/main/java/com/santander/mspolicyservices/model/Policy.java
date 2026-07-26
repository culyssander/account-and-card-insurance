package com.santander.mspolicyservices.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "policy_id_seq")
    @SequenceGenerator(
            name = "policy_id_seq",
            sequenceName = "policy_id_seq",
            allocationSize = 1
    )
    private BigInteger id;

    @Column(name = "policy_number")
    private String policyNumber;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "product_id")
    private BigInteger productId;
    private String  cpf;
    private String status;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

}
