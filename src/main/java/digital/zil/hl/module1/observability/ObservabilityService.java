package digital.zil.hl.module1.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ObservabilityService {

    private static final Logger log = LoggerFactory.getLogger(ObservabilityService.class);

    private final TimingRegistry registry;

    @Value("${observability.window.short:10}")
    private int shortWindowSec;

    @Value("${observability.window.medium:30}")
    private int mediumWindowSec;

    @Value("${observability.window.long:60}")
    private int longWindowSec;

    public ObservabilityService(TimingRegistry registry) {
        this.registry = registry;
    }

    @Scheduled(fixedRateString = "${observability.schedule.ms:10000}")
    public void reportStats() {
        registry.evict(longWindowSec);
        Instant now = Instant.now();
        log.info("=== Observability Report @ {} ===", now);

        for (int window : List.of(shortWindowSec, mediumWindowSec, longWindowSec)) {
            var windowEvents = registry.getWindow(window);
            var grouped = windowEvents.stream()
                    .collect(Collectors.groupingBy(
                            TimingRegistry.TimingEvent::operation,
                            Collectors.summarizingLong(TimingRegistry.TimingEvent::durationMs)
                    ));

            log.info("  [{}s] total={}", window, windowEvents.size());
            grouped.forEach((op, stats) ->
                    log.info("    {} -> count={} avg={}ms min={}ms max={}ms",
                            op, stats.getCount(), (long) stats.getAverage(), stats.getMin(), stats.getMax())
            );
        }
        log.info("=== End Report ===");
    }
}