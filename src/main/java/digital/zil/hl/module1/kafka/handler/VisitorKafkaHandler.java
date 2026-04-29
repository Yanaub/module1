package digital.zil.hl.module1.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digital.zil.hl.module1.model.Visitor;
import digital.zil.hl.module1.observability.ObservabilityService;
import digital.zil.hl.module1.service.VisitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VisitorKafkaHandler implements EntityKafkaHandler {
    private static final Logger log = LoggerFactory.getLogger(ObservabilityService.class);
    private final VisitorService visitorService;
    private final ObjectMapper objectMapper;

    public VisitorKafkaHandler(VisitorService visitorService, ObjectMapper objectMapper) {
        this.visitorService = visitorService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String entityType() {
        return "VISITOR";
    }

    @Override
    public void handle(String operation, String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);

            switch (operation.toUpperCase()) {
                case "POST" -> handleCreate(node);
                case "PUT" -> handleUpdate(node);
                case "DEL" -> handleDelete(node);
                default -> log.warn("Unsupported operation '{}' for VISITOR", operation);
            }
        } catch (Exception e) {
            log.error("Failed to handle VISITOR payload: {}", payload, e);
        }
    }

    private void handleCreate(JsonNode node) {
        Visitor visitor = new Visitor();
        visitor.setIdentifier(UUID.fromString(node.get("identifier").asText()));
        visitor.setFullName(node.get("fullName").asText());
        visitor.setAge(node.get("age").asInt());
        visitor.setTicketType(Visitor.TicketType.valueOf(node.get("ticketType").asText()));

        visitorService.saveVisitor(visitor);
        log.info("Kafka CREATE VISITOR: {}", visitor);
    }

    private void handleUpdate(JsonNode node) {
        String id = node.get("identifier").asText();

        Visitor visitor = new Visitor();
        visitor.setIdentifier(UUID.fromString(id));
        visitor.setFullName(node.get("fullName").asText());
        visitor.setAge(node.get("age").asInt());
        visitor.setTicketType(Visitor.TicketType.valueOf(node.get("ticketType").asText()));

        visitorService.updateVisitor(id, visitor);
        log.info("Kafka UPDATE VISITOR: {}", visitor);
    }

    private void handleDelete(JsonNode node) {
        String id = node.get("identifier").asText();
        visitorService.deleteVisitor(id);
        log.info("Kafka DELETE VISITOR id={}", id);
    }
}