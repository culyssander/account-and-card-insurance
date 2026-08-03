package com.santander.msclaimsquestionnaireservices.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "question_option",
        uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "option_code"}))
public class QuestionOptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity question;

    @Column(name = "option_code", nullable = false, length = 60)
    private String optionCode;

    @Column(nullable = false, length = 255)
    private String label;

    @Column(name = "next_question_code", length = 10)
    private String nextQuestionCode;

    @Column(name = "outcome_code", length = 60)
    private String outcomeCode;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;


    public QuestionOptionEntity(QuestionEntity question, String optionCode, String label,
                                 String nextQuestionCode, String outcomeCode, int displayOrder) {
        this.question = question;
        this.optionCode = optionCode;
        this.label = label;
        this.nextQuestionCode = nextQuestionCode;
        this.outcomeCode = outcomeCode;
        this.displayOrder = displayOrder;
    }

}
