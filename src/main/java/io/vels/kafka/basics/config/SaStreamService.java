package io.vels.kafka.basics.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;
import java.util.function.Function;

@Configuration
@EnableAutoConfiguration
@Slf4j
public class SaStreamService {

    @Bean
    public Function<KStream<String, String>, KStream<String, String>> upper() {
        return input -> input.mapValues( i -> {
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
}
