package com.santander.msclaimsquestionnaireservices.repository;

import com.santander.msclaimsquestionnaireservices.model.AnsweredQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface AnsweredQuestionRepository extends JpaRepository<AnsweredQuestion, BigInteger> {
    List<AnsweredQuestion> findAllBySessionClaimId(String sessionClaimId);
}
