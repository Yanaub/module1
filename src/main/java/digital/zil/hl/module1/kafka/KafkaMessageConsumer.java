package digital.zil.hl.module1.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import digital.zil.hl.module1.kafka.dto.KafkaMessage;
import digital.zil.hl.module1.kafka.handler.EntityKafkaHandler;
import digital.zil.hl.module1.observability.ObservabilityService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KafkaMessageConsumer {

    private final ObjectMapper objectMapper;
    private final Map<String, EntityKafkaHandler> handlers;



    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "${app.kafka.concurrency}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload String rawMessage,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
    ) {
        log.info("Received message from partition {}: {}", partition, rawMessage);

        try {
            KafkaMessage message = objectMapper.readValue(rawMessage, KafkaMessage.class);

            String entity = message.getEntity() == null ? "" : message.getEntity().toUpperCase();
            String operation = message.getOperation();
            String payload = message.getPayload();

            EntityKafkaHandler handler = handlers.get(entity);

            if (handler == null) {
                log.warn("Unsupported Kafka entity '{}', operation='{}', payload={}",
                        entity, operation, payload);
                return;
            }

            handler.handle(operation, payload);

        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", rawMessage, e);
        }
    }
}