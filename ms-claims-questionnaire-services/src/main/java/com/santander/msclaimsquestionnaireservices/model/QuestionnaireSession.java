package com.santander.msclaimsquestionnaireservices.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questionnaire_session")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireSession {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "questionnaire_session_id_seq")
    private Long id;

    @Column(name = "claim_id")
    private String claimId;

    @Column(name = "current_question_id")
    private String currentQuestionId;

    @OneToMany(
            mappedBy = "session",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AnsweredQuestion> answers = new ArrayList<>();

    public void addAnswer(AnsweredQuestion answer) {
        answers.add(answer);
    }

    public void removeAnswer(AnsweredQuestion answer) {
        answers.remove(answer);
    }
}
