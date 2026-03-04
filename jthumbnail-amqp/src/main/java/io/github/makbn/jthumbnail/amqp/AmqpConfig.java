package io.github.makbn.jthumbnail.amqp;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AMQP connector configuration: exchange, queue, DLQ, and listener factory with manual ack and retry.
 */
@Configuration
@ConditionalOnProperty(name = "jthumbnailer.amqp.enabled", havingValue = "true")
@EnableConfigurationProperties(AmqpProperties.class)
public class AmqpConfig {

    @Bean
    public MessageConverter amqpMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange thumbnailExchange(AmqpProperties props) {
        return new DirectExchange(props.exchange(), true, false);
    }

    @Bean
    public Queue thumbnailQueue(AmqpProperties props) {
        return QueueBuilder.durable(props.queue())
                .withArgument("x-dead-letter-exchange", props.deadLetterExchange())
                .withArgument("x-dead-letter-routing-key", props.deadLetterQueue())
                .build();
    }

    @Bean
    public Binding thumbnailBinding(Queue thumbnailQueue, DirectExchange thumbnailExchange, AmqpProperties props) {
        return BindingBuilder.bind(thumbnailQueue).to(thumbnailExchange).with(props.routingKey());
    }

    @Bean
    public DirectExchange deadLetterExchange(AmqpProperties props) {
        return new DirectExchange(props.deadLetterExchange(), true, false);
    }

    @Bean
    public Queue deadLetterQueue(AmqpProperties props) {
        return new Queue(props.deadLetterQueue(), true);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange, AmqpProperties props) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(props.deadLetterQueue());
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, AmqpProperties props, MessageConverter amqpMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(amqpMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(props.consumerConcurrency());
        factory.setPrefetchCount(1);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter amqpMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(amqpMessageConverter);
        return template;
    }
}
