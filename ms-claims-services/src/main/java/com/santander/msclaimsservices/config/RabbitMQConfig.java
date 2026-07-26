package com.santander.msclaimsservices.config;

import com.santander.msclaimsservices.constants.ClaimConstants;
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
    public Queue loggingQueue() {
        return QueueBuilder.durable(ClaimConstants.RABBIT_QUEUE_LOGGING).build();
    }

    @Bean
    public DirectExchange loggingExchange() {
        return ExchangeBuilder.directExchange(ClaimConstants.RABBIT_QUEUE_LOGGING_EXCHANGE).build();
    }

    @Bean
    Binding loggingBinding(Queue loggingQueue, DirectExchange loggingExchange, AmqpAdmin amqpAdmin){
        amqpAdmin.declareExchange(loggingExchange);
        amqpAdmin.declareQueue(loggingQueue);
        var binding = BindingBuilder.bind(loggingQueue).to(loggingExchange).with(ClaimConstants.RABBIT_QUEUE_LOGGING_ROUTER);
        amqpAdmin.declareBinding(binding);
        return binding;
    }

    @Bean
    public Queue claimQueue() {
        return QueueBuilder.durable(ClaimConstants.RABBIT_QUEUE_CLAIM).build();
    }

    @Bean
    public DirectExchange claimExchange() {
        return ExchangeBuilder.directExchange(ClaimConstants.RABBIT_QUEUE_CLAIM_EXCHANGE).build();
    }

    @Bean
    Binding claimBinding(Queue claimQueue, DirectExchange claimExchange, AmqpAdmin amqpAdmin){
        amqpAdmin.declareExchange(claimExchange);
        amqpAdmin.declareQueue(claimQueue);
        var binding = BindingBuilder.bind(claimQueue).to(claimExchange).with(ClaimConstants.RABBIT_QUEUE_CLAIM_ROUTER);
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