package io.vels.kafka.basics.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Configuration
@EnableAutoConfiguration
@Slf4j
public class SaStreamService {

    private Set<String> blackList = Set.of("bug", "problem", "issue");

    @Bean
    public Function<KStream<String, String>, KStream<String, String>> upper() {
        return input -> input
                .peek((k,v) -> {
                    Arrays.stream(v.split(" ")).forEach( w -> {
                        if (blackList.contains(w)) {
                            throw new RuntimeException("Message contains a blacklisted word ! ");
                        }
                    });
                })
                .mapValues( i -> {
                    String toReturn = i.toUpperCase(Locale.getDefault());
                    log.info("Event: {}", toReturn);
                    return toReturn;
                });
    }

    @Bean
    public Function<KStream<String, String>, KStream<String, String>> ending() {
        return input -> input.mapValues( i -> {
            String toReturn = String.format("%s !", i);
            log.info("Ending: {}", toReturn);
            return toReturn;
        });
    }

    @Bean
    public Function<KStream<String, String>, KStream<String, String>> process() {
        return upper().andThen(ending());
    }

    @Bean
    Function<Message<String>, Message<String>> transform() {
        return m -> {
            log.info("[transform()]-> transform() called");
            checkForBlackListedWord(m.getPayload());
            return MessageBuilder
                    .withPayload(m.getPayload())
                    .copyHeaders(m.getHeaders())
                    .build();
        };
    }

    private void checkForBlackListedWord(final String message) {
        Arrays.stream(message.split(" ")).forEach( w -> {
            if (blackList.contains(w)) {
                throw new RuntimeException("Message contains a blacklisted word ! ");
            }
        });
    }

    @Bean
    Supplier<Flux<Message<?>>> dlq() {
        return () -> {
            log.warn("dlq supplier called (should happen only once)");
            return Flux.empty();
        };
    }

    @ServiceActivator(inputChannel = "input-topic.wordGroup.errors", outputChannel = "dlq-out-0")
    public Message<?> handleError(@Header("id") String id, @NotNull ErrorMessage message) {
        Message<?> original = message.getOriginalMessage();
        log.error("Handling exception via error channel, id='{}', message='{}'", id, original);
        return original;
    }
}
