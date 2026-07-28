package com.santander.msclaimsquestionnaireservices.repository;

import com.santander.msclaimsquestionnaireservices.model.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    // JOIN FETCH pra trazer question + options numa query só — sem isso,
    // cada acesso a getOptions() fora da transação vira
    // LazyInitializationException (ou, se a sessão ainda estiver aberta,
    // um SELECT N+1 silencioso).
    @Query("select q from QuestionEntity q left join fetch q.options where q.code = :code")
    Optional<QuestionEntity> findByCodeFetchingOptions(@Param("code") String code);
}
