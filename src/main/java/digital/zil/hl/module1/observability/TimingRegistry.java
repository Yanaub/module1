package digital.zil.hl.module1.observability;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class TimingRegistry {

    public record TimingEvent(String operation, long durationMs, Instant timestamp) {}

    private final ConcurrentLinkedQueue<TimingEvent> events = new ConcurrentLinkedQueue<>();

    public void record(String operation, long startNano) {
        long durationMs = (System.nanoTime() - startNano) / 1_000_000;
        events.add(new TimingEvent(operation, durationMs, Instant.now()));
    }

    public List<TimingEvent> getWindow(int seconds) {
        Instant cutoff = Instant.now().minusSeconds(seconds);
        return events.stream()
                .filter(e -> e.timestamp().isAfter(cutoff))
                .toList();
    }

    public void evict(int maxSeconds) {
        Instant cutoff = Instant.now().minusSeconds(maxSeconds);
        events.removeIf(e -> e.timestamp().isBefore(cutoff));
    }
}