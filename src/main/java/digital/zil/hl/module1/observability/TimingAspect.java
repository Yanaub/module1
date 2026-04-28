package digital.zil.hl.module1.observability;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimingAspect {

    private final TimingRegistry registry;

    public TimingAspect(TimingRegistry registry) {
        this.registry = registry;
    }


    @Around("within(@org.springframework.web.bind.annotation.RestController *)" +
            " || within(@org.springframework.stereotype.Service *)" +
            " || within(@org.springframework.stereotype.Repository *)")
    public Object time(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            String op = pjp.getSignature().getDeclaringTypeName()
                    + "." + pjp.getSignature().getName();
            registry.record(op, start);
        }
    }
}