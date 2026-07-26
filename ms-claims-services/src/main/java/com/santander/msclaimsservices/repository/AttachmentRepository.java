package com.santander.msclaimsservices.repository;

import com.santander.msclaimsservices.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface AttachmentRepository extends JpaRepository<Attachment, BigDecimal> {
}
