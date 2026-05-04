package com.bsr.config;

import java.util.List;

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
    public static final String TASK_UPDATE_QUEUE = "tasks.update";
    public static final String TASK_DELETE_QUEUE = "tasks.delete";
    public static final String TASK_REVIEW_QUEUE = "tasks.review";
    public static final String TASK_COMMENT_QUEUE = "tasks.comment";
    public static final String TASK_EXCHANGE = "tasks.exchange";
    public static final String TASK_CREATE_ROUTING_KEY = "tasks.create";
    public static final String TASK_UPDATE_ROUTING_KEY = "tasks.update";
    public static final String TASK_DELETE_ROUTING_KEY = "tasks.delete";
    public static final String TASK_REVIEW_ROUTING_KEY = "tasks.review";
    public static final String TASK_COMMENT_ROUTING_KEY = "tasks.comment";
    public static final String TASK_CREATE_LISTENER_ID = "taskCreateListener";
    public static final String TASK_UPDATE_LISTENER_ID = "taskUpdateListener";
    public static final String TASK_DELETE_LISTENER_ID = "taskDeleteListener";
    public static final String TASK_REVIEW_LISTENER_ID = "taskReviewListener";
    public static final String TASK_COMMENT_LISTENER_ID = "taskCommentListener";
    public static final List<String> WRITE_LISTENER_IDS = List.of(
            TASK_CREATE_LISTENER_ID,
            TASK_UPDATE_LISTENER_ID,
            TASK_DELETE_LISTENER_ID,
            TASK_REVIEW_LISTENER_ID,
            TASK_COMMENT_LISTENER_ID
    );

    @Bean
    public Queue taskCreateQueue() {
        return new Queue(TASK_CREATE_QUEUE, true);
    }

    @Bean
    public Queue taskUpdateQueue() {
        return new Queue(TASK_UPDATE_QUEUE, true);
    }

    @Bean
    public Queue taskDeleteQueue() {
        return new Queue(TASK_DELETE_QUEUE, true);
    }

    @Bean
    public Queue taskReviewQueue() {
        return new Queue(TASK_REVIEW_QUEUE, true);
    }

    @Bean
    public Queue taskCommentQueue() {
        return new Queue(TASK_COMMENT_QUEUE, true);
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
    public Binding taskUpdateBinding(Queue taskUpdateQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(taskUpdateQueue).to(taskExchange).with(TASK_UPDATE_ROUTING_KEY);
    }

    @Bean
    public Binding taskDeleteBinding(Queue taskDeleteQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(taskDeleteQueue).to(taskExchange).with(TASK_DELETE_ROUTING_KEY);
    }

    @Bean
    public Binding taskReviewBinding(Queue taskReviewQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(taskReviewQueue).to(taskExchange).with(TASK_REVIEW_ROUTING_KEY);
    }

    @Bean
    public Binding taskCommentBinding(Queue taskCommentQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(taskCommentQueue).to(taskExchange).with(TASK_COMMENT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
