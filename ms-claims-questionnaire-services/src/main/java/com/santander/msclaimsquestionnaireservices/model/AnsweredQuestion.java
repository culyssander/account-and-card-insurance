package com.santander.msclaimsquestionnaireservices.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "answered_question")
public class AnsweredQuestion {
    @Id
    private BigInteger id;

    @Column(name = "questionnaire_id")
    private BigInteger questionnaireId;

//    @Column(name = "claim_id")
//    private String claimId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id")
    private QuestionnaireSession session;

    @Column(name = "question_id")
    private String questionId;
    private String answer;

    @Column(name = "next_question_id")
    private String nextQuestionId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
