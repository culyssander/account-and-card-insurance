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

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attachment_id_seq")
    private BigInteger id;

    @Column(name = "claim_id")
    private BigInteger claimId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "document_type")
    private String documentType;
    private String url;
    private long size;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
}
