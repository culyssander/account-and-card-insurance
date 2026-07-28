package com.santander.msclaimsquestionnaireservices.flow;

/**
 * Porta: de onde a árvore de perguntas vem é um detalhe de infraestrutura.
 * O QuestionnaireService depende só disso — trocar a fonte (banco, cache,
 * arquivo de config) não deveria exigir mudar uma linha do service.
 */
public interface QuestionFlow {

    QuestionNode start();

    QuestionNode nodeById(String questionId);
}
