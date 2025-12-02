package com.flow.mq;

import com.flow.model.dto.FileProcessingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    // 交换机和路由键常量 - 假设使用默认值或配置值
    // 为了简单起见，我们暂时使用直接队列或主题交换机。
    // 假设现在使用简单队列，或者使用 flow-rabbitmq-starter 中的配置。
    // 查看依赖项，使用了 flow-rabbitmq-starter。
    // 我暂时在这里定义常量，稍后可以移动到配置中。
    public static final String EXCHANGE_NAME = "flow.file.exchange";
    public static final String ROUTING_KEY = "flow.file.process";

    public void sendFileProcessingMessage(FileProcessingMessage message) {
        log.info("Sending file processing message: {}", message);
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, message);
    }
}
