package digital.zil.hl.module1.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digital.zil.hl.module1.model.Excursion;
import digital.zil.hl.module1.service.ExcursionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component("EXCURSION")
@RequiredArgsConstructor
public class ExcursionKafkaHandler implements EntityKafkaHandler {

    private final ExcursionService excursionService;
    private final ObjectMapper objectMapper;

    @Override
    public String entityType() {
        return "EXCURSION";
    }

    @Override
    public void handle(String operation, String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);

            switch (operation.toUpperCase()) {
                case "POST" -> handleCreate(node);
                case "PUT" -> handleUpdate(node);
                case "DEL" -> handleDelete(node);
                default -> log.warn("Unsupported operation '{}' for EXCURSION", operation);
            }
        } catch (Exception e) {
            log.error("Failed to handle EXCURSION payload: {}", payload, e);
        }
    }

    private void handleCreate(JsonNode node) {
        Excursion excursion = new Excursion();
        excursion.setIdentifier(UUID.fromString(node.get("identifier").asText()));
        excursion.setDate(LocalDate.parse(node.get("date").asText()));
        excursion.setGuide(node.get("guide").asText());

        excursionService.saveExcursion(excursion);
        log.info("Kafka CREATE EXCURSION: {}", excursion);
    }

    private void handleUpdate(JsonNode node) {
        String id = node.get("identifier").asText();

        Excursion excursion = new Excursion();
        excursion.setIdentifier(UUID.fromString(id));
        excursion.setDate(LocalDate.parse(node.get("date").asText()));
        excursion.setGuide(node.get("guide").asText());

        excursionService.updateExcursion(id, excursion);
        log.info("Kafka UPDATE EXCURSION: {}", excursion);
    }

    private void handleDelete(JsonNode node) {
        String id = node.get("identifier").asText();
        excursionService.deleteExcursion(id);
        log.info("Kafka DELETE EXCURSION id={}", id);
    }
}