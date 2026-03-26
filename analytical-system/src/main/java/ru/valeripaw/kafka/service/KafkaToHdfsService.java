package ru.valeripaw.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Service;
import ru.valeripaw.kafka.dto.ClientRequestAvro;
import ru.valeripaw.kafka.dto.ProductAvro;
import ru.valeripaw.kafka.properties.HdfsProperties;
import ru.valeripaw.kafka.properties.KafkaProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Читает топики и пишет Avro в HDFS.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaToHdfsService {

    private final KafkaConsumer<String, ProductAvro> productConsumer;
    private final KafkaConsumer<String, ClientRequestAvro> clientRequestConsumer;
    private final HdfsProperties hdfsProperties;
    private final KafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void moveData() throws IOException {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", hdfsProperties.getNamenode());
        conf.setInt("dfs.replication", 1);

        FileSystem fs = FileSystem.get(conf);

        log.info("Writing to: {}", fs.getUri());

        try {
            ExecutorService executorProducts = Executors.newSingleThreadExecutor();
            executorProducts.execute(() -> {
                try {
                    log.info("Start consuming allowed-products");
                    moveProducts(fs);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            });

            ExecutorService executorClientRequests = Executors.newSingleThreadExecutor();
            executorClientRequests.execute(() -> {
                try {
                    log.info("Start consuming client-requests");
                    moveClientRequests(fs);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        }
    }

    private void moveClientRequests(FileSystem fs) throws IOException {
        clientRequestConsumer.subscribe(List.of(kafkaProperties.getClientRequestsTopic().getTopic()));

        while (true) {
            ConsumerRecords<String, ClientRequestAvro> records = clientRequestConsumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, ClientRequestAvro> record : records) {
                writeToFile(fs, record);
            }
        }
    }

    private void moveProducts(FileSystem fs) throws IOException {
        productConsumer.subscribe(List.of(kafkaProperties.getAllowedProductsTopic().getTopic()));

        while (true) {
            ConsumerRecords<String, ProductAvro> records = productConsumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, ProductAvro> record : records) {
                writeToFile(fs, record);
            }
        }
    }

    public <T extends SpecificRecord> void writeToFile(
            FileSystem hdfs,
            ConsumerRecord<String, T> record
    ) throws IOException {
        String topic = record.topic();
        T value = record.value();

        String path = "/data/" + topic + "/" + System.currentTimeMillis() + ".json";
        log.info("writeToJsonFile\nrecord {}\nfile {}", value, path);

        Path filePath = new Path(path);
        hdfs.mkdirs(filePath.getParent());

        try (FSDataOutputStream out = hdfs.create(filePath, true)) {
            // Конвертируем Avro SpecificRecord в Map
            Map<String, Object> map = convertAvroToMap(value);
            map.remove("images");
            map.remove("tags");
            // Сериализуем в JSON и записываем в HDFS
            String json = objectMapper.writeValueAsString(map);
            out.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Failed to write record to HDFS at path {}: {}", filePath, e.getMessage(), e);
            throw e;
        }
    }

    private <T extends SpecificRecord> Map<String, Object> convertAvroToMap(T record) {
        Map<String, Object> map = new HashMap<>();
        record.getSchema().getFields().forEach(field -> {
            Object value = record.get(field.pos());
            if (value instanceof SpecificRecord) {
                value = convertAvroToMap((SpecificRecord) value);
            }
            map.put(field.name(), value);
        });
        return map;
    }

}
