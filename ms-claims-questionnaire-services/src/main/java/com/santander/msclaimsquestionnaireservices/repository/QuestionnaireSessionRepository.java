package com.santander.msclaimsquestionnaireservices.repository;

import com.santander.msclaimsquestionnaireservices.model.QuestionnaireSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface QuestionnaireSessionRepository extends JpaRepository<QuestionnaireSession, BigInteger> {

    boolean existsByClaimIdAndCurrentQuestionId(String claimId, String currentQuestionId);
    Optional<QuestionnaireSession> findByClaimIdAndCurrentQuestionId(String claimId, String currentQuestionId);
}
