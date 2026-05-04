package pl.projekt.backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
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

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter,
            @Value("${tasks.rabbitmq.reply-timeout-ms:10000}") long replyTimeoutMs) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        rabbitTemplate.setReplyTimeout(replyTimeoutMs);
        return rabbitTemplate;
    }
}
