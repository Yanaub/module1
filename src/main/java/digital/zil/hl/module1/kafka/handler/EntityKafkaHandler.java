package digital.zil.hl.module1.kafka.handler;

public interface EntityKafkaHandler {
    String entityType();
    void handle(String operation, String payload);
}