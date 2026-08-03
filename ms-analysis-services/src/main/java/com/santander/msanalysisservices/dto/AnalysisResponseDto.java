package com.santander.msanalysisservices.dto;

import com.santander.msanalysisservices.model.AnalysisResult;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponseDto implements Serializable {

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
