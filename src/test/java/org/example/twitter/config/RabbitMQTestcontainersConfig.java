package org.example.twitter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@TestConfiguration(proxyBeanMethods = false)
@Slf4j
public class RabbitMQTestcontainersConfig {
    @Bean
    @ServiceConnection
    public RabbitMQContainer rabbitMQContainer() {
        RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management")
                .waitingFor(Wait.forLogMessage(".*Server startup complete.*\\n", 1));

        rabbitMQContainer.start();
        return rabbitMQContainer;
    }
}
