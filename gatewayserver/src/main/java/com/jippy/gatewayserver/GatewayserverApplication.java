package com.jippy.gatewayserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration.class
})

public class GatewayserverApplication {

	public static void main(String[] args) {

        // CRITICAL: Instructs Project Reactor to pass Tracing data across non-blocking thread hops
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(GatewayserverApplication.class, args);
	}

}
