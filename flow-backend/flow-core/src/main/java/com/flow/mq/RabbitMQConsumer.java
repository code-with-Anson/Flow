package com.flow.mq;

import com.flow.model.dto.FileProcessingMessage;
import com.flow.service.MultimodalSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final MultimodalSearchService multimodalSearchService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "flow.file.queue", durable = "true"), exchange = @Exchange(value = RabbitMQProducer.EXCHANGE_NAME), key = RabbitMQProducer.ROUTING_KEY))
    public void receiveFileProcessingMessage(FileProcessingMessage message) {
        log.info("Received file processing message: {}", message);
        try {
            multimodalSearchService.processFile(message);
        } catch (Exception e) {
            log.error("Error processing file message: {}", message, e);
            // 根据需求，可能想要拒绝/重新排队或进入死信队列 (DLQ)
        }
    }
}
