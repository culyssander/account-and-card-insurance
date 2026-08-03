package com.santander.msanalysisservices.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analysis_id_seq")
    private BigInteger id;

    @Column(name = "claim_id")
    private String claimId;

    @Column(name = "analyst_id")
    private BigInteger analystId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AnalysisResult result;

    @Column(name = "reason_for_denial")
    private String reasonForDenial;

    @Column(name = "compensation_amount")
    private BigDecimal compensationAmount;

    @Column(name = "analysis_date")
    private LocalDateTime analysisDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
