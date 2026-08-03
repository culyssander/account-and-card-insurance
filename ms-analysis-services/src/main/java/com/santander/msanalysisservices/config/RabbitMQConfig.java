package com.santander.msanalysisservices.config;

import com.santander.msanalysisservices.constants.AnalysisConstants;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile("!test")
public class RabbitMQConfig {

    @Bean
    public Queue analysisQueue() {
        return QueueBuilder.durable(AnalysisConstants.RABBIT_QUEUE_ANALYSIS).build();
    }

    @Bean
    public DirectExchange analysisExchange() {
        return ExchangeBuilder.directExchange(AnalysisConstants.RABBIT_QUEUE_ANALYSIS_EXCHANGE).build();
    }

    @Bean
    Binding analysisBinding(Queue analysisQueue, DirectExchange analysisExchange, AmqpAdmin amqpAdmin){
        amqpAdmin.declareExchange(analysisExchange);
        amqpAdmin.declareQueue(analysisQueue);
        var binding = BindingBuilder.bind(analysisQueue).to(analysisExchange).with(AnalysisConstants.RABBIT_QUEUE_ANALYSIS_ROUTER);
        amqpAdmin.declareBinding(binding);
        return binding;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

}