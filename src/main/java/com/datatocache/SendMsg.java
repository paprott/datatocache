package com.datatocache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class SendMsg {

    private static final Logger log = LoggerFactory.getLogger(SendMsg.class);

    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory =
                new CachingConnectionFactory("localhost");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }


    public void send() throws Exception {
        log.info("Sending message...");
        rabbitTemplate().convertAndSend(DatatocacheApplicationConfiguration.topicExchangeName, "nordea.task", "LOAD");
    }
}
