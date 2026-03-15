package ru.valeripaw.kafka.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "hdfs")
public class HdfsProperties {

    private String namenode;

}
