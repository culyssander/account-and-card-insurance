package com.santander.msclaimsquestionnaireservices.config;

import com.santander.msclaimsquestionnaireservices.constants.QuestionnaireConstants;
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
    public Queue questionnaireQueue() {
        return QueueBuilder.durable(QuestionnaireConstants.RABBIT_QUEUE_QUESTIONNAIRE_COMPLETE).build();
    }

    @Bean
    public DirectExchange questionnaireExchange() {
        return ExchangeBuilder.directExchange(QuestionnaireConstants.RABBIT_QUESTIONNAIRE_COMPLETE_EXCHANGE).build();
    }

    @Bean
    Binding questionnaireBinding(Queue questionnaireQueue, DirectExchange questionnaireExchange, AmqpAdmin amqpAdmin){
        amqpAdmin.declareExchange(questionnaireExchange);
        amqpAdmin.declareQueue(questionnaireQueue);
        var binding = BindingBuilder.bind(questionnaireQueue).to(questionnaireExchange).with(QuestionnaireConstants.RABBIT_QUEUE_QUESTIONNAIRE_COMPLETE_ROUTER);
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