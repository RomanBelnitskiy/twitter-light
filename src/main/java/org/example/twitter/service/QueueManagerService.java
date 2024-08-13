package org.example.twitter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueManagerService {

    private final RabbitAdmin rabbitAdmin;
    private final DirectExchange directExchange;

    public void createQueueForUser(final String userId) {
        Queue queue = QueueBuilder.durable(userId).build();
        Binding binding = new Binding(userId, Binding.DestinationType.QUEUE,
                directExchange.getName(),userId, null);

        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);
    }

    public void deleteQueueForUser(final String userId) {
        Binding binding = new Binding(userId, Binding.DestinationType.QUEUE,
                directExchange.getName(), userId, null);
        rabbitAdmin.removeBinding(binding);
        rabbitAdmin.deleteQueue(userId);
    }
}
