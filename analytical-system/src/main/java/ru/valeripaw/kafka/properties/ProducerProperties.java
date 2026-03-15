package ru.valeripaw.kafka.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProducerProperties {

    private String topic;
    private String acks;
    private int retries;
    private long retryBackoffMs;
    private boolean enableIdempotence;
}
