package com.jippy.gatewayserver.config;

import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactiveLoggingConfig {

    @PostConstruct
    public void init() {
        // Bridges your thread-bound Logback MDC with Project Reactor's async pipeline
        ContextRegistry.getInstance().registerThreadLocalAccessor(
                "mdc-context",
                MDC::getCopyOfContextMap,
                MDC::setContextMap,
                MDC::clear
        );
    }
}
