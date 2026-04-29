package digital.zil.hl.module1.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digital.zil.hl.module1.model.Exhibit;
import digital.zil.hl.module1.observability.ObservabilityService;
import digital.zil.hl.module1.service.ExhibitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExhibitKafkaHandler implements EntityKafkaHandler {
    private static final Logger log = LoggerFactory.getLogger(ObservabilityService.class);
    private final ExhibitService exhibitService;
    private final ObjectMapper objectMapper;

    public ExhibitKafkaHandler(ExhibitService exhibitService, ObjectMapper objectMapper) {
        this.exhibitService = exhibitService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String entityType() {
        return "EXHIBIT";
    }

    @Override
    public void handle(String operation, String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);

            switch (operation.toUpperCase()) {
                case "POST" -> handleCreate(node);
                case "PUT" -> handleUpdate(node);
                case "DEL" -> handleDelete(node);
                default -> log.warn("Unsupported operation '{}' for EXHIBIT", operation);
            }
        } catch (Exception e) {
            log.error("Failed to handle EXHIBIT payload: {}", payload, e);
        }
    }

    private void handleCreate(JsonNode node) {
        Exhibit exhibit = new Exhibit();
        exhibit.setIdentifier(UUID.fromString(node.get("identifier").asText()));
        exhibit.setName(node.get("name").asText());
        exhibit.setEpoch(node.get("epoch").asText());
        exhibit.setDescription(node.get("description").asText());

        exhibitService.saveExhibit(exhibit);
        log.info("Kafka CREATE EXHIBIT: {}", exhibit);
    }

    private void handleUpdate(JsonNode node) {
        String id = node.get("identifier").asText();

        Exhibit exhibit = new Exhibit();
        exhibit.setIdentifier(UUID.fromString(id));
        exhibit.setName(node.get("name").asText());
        exhibit.setEpoch(node.get("epoch").asText());
        exhibit.setDescription(node.get("description").asText());

        exhibitService.updateExhibit(id, exhibit);
        log.info("Kafka UPDATE EXHIBIT: {}", exhibit);
    }

    private void handleDelete(JsonNode node) {
        String id = node.get("identifier").asText();
        exhibitService.deleteExhibit(id);
        log.info("Kafka DELETE EXHIBIT id={}", id);
    }
}