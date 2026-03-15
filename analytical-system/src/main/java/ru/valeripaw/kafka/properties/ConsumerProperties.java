package ru.valeripaw.kafka.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsumerProperties {

    private String topic;
    private String groupId;
    private boolean enable;
    private boolean enableAutoCommit;
    private int autoCommitIntervalMs;
    private int maxPollRecords;
    private long pollDurationMs;

}
