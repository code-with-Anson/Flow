package com.flow.mq;

import com.flow.model.dto.FileProcessingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${flow.mq.exchange-name}")
    private String exchangeName;

    @Value("${flow.mq.routing-key}")
    private String routingKey;

    public void sendFileProcessingMessage(FileProcessingMessage message) {
        log.info("Sending file processing message: {}", message);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
    }
}
