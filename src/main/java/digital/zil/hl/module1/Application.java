package digital.zil.hl.module1;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private final MeterRegistry meterRegistry;

    public Application(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logTomcatThreadsOnStartup() {
        logTomcatThreads();
    }

    @Scheduled(fixedDelayString = "${monitor.tomcat-threads.delay:5000}")
    public void logTomcatThreads() {
        double current = getGauge("tomcat.threads.current");
        double busy = getGauge("tomcat.threads.busy");
        double max = getGauge("tomcat.threads.config.max");

        log.info("Tomcat threads: current={}, busy={}, max={}",
                (int) current, (int) busy, (int) max);
    }

    private double getGauge(String name) {
        var gauge = meterRegistry.find(name).gauge();
        return gauge != null ? gauge.value() : Double.NaN;
    }
}