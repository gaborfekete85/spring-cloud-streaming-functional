package io.vels.kafka.basics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.function.Supplier;

@SpringBootApplication
@Slf4j
@Controller
@ComponentScan("io.vels.kafka.basics")
public class KafkaBasicProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaBasicProducerApplication.class, args);
    }

    @Autowired
    private StreamBridge streamBridge;

    @Bean
    public Supplier<String> numberProducer() {
        return () -> {
            System.out.println("Sending ... ");
            return "Hello Gaben";
        };
    }

    @RequestMapping("/kafka")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void delegateToSupplier(@RequestBody String body) {
        System.out.println("Sending " + body);
        streamBridge.send("numberProducer-out-0", body);
    }
}
