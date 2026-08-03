package com.santander.msnotificationservices.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.santander.msnotificationservices.constants.NotificationConstants;
import com.santander.msnotificationservices.dto.ClaimResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class RabbitMQListener {

    private final ElasticsearchClient elasticsearchClient;

    @RabbitListener(queues = NotificationConstants.RABBIT_QUEUE_CLAIM)
    public void claimStatus(ClaimResponseDto message) {
        log.info("SEND EMAIL ABOUT CLAIM: {} ", message);
    }

    @RabbitListener(queues = NotificationConstants.RABBIT_QUEUE_ANALYSIS)
    public void analysis(Message message) {
        log.info("SEND EMAIL ABOUT ANALYSIS: {} ", message);
    }

    @RabbitListener(queues = NotificationConstants.RABBIT_QUEUE_LOGGING)
    public void logging(Map<String, Object> logDocument) {
        try {
            elasticsearchClient.index(i -> i
                    .index("insurance-account-card")
                    .document(logDocument)
            );
        } catch (IOException e) {
            log.error("Falha ao indexar log no Elasticsearch", e);
        }
    }
}