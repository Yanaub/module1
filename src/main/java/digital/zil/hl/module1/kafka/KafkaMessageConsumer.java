package digital.zil.hl.module1.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import digital.zil.hl.module1.kafka.dto.KafkaMessage;
import digital.zil.hl.module1.kafka.handler.EntityKafkaHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.HashSet;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageConsumer {

    private final Map<String, EntityKafkaHandler> handlers;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "${app.kafka.concurrency}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    
    public void consumeBatch(
            @Payload List<String> rawMessages,
            @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions
    ) {
        log.info("Received batch: {} messages from partitions: {}", 
                 rawMessages.size(), new HashSet<>(partitions));

        for (int i = 0; i < rawMessages.size(); i++) {
            try {
                String rawMessage = rawMessages.get(i);
  

                KafkaMessage message = objectMapper.readValue(rawMessage, KafkaMessage.class);

                String entity = message.getEntity() == null ? "" : message.getEntity().toUpperCase();
                String operation = message.getOperation();
                String payload = message.getPayload();

                EntityKafkaHandler handler = handlers.get(entity);

                if (handler == null) {
                    log.warn("Unsupported Kafka entity '{}', operation='{}'", entity, operation);
                    continue; 
                }

                handler.handle(operation, payload);

            } catch (Exception e) {
                log.error("Failed to process message #{} in batch", i, e);
            }
        }
    }
}