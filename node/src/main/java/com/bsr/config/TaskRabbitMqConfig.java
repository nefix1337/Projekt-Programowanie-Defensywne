package com.bsr.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskRabbitMqConfig {
    public static final String TASK_CREATE_QUEUE = "tasks.create";
    public static final String TASK_EXCHANGE = "tasks.exchange";
    public static final String TASK_CREATE_ROUTING_KEY = "tasks.create";
    public static final String TASK_CREATE_LISTENER_ID = "taskCreateListener";

    @Bean
    public Queue taskCreateQueue() {
        return new Queue(TASK_CREATE_QUEUE, true);
    }

    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange(TASK_EXCHANGE, true, false);
    }

    @Bean
    public Binding taskCreateBinding(Queue taskCreateQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(taskCreateQueue).to(taskExchange).with(TASK_CREATE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
