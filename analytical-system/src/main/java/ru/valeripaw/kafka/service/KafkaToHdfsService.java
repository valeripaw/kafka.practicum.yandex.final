package ru.valeripaw.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
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
import java.time.Duration;
import java.util.List;
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

    public void moveData() throws IOException {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", hdfsProperties.getNamenode());

        FileSystem fs = FileSystem.get(conf);

        log.info("Writing to: {}", fs.getUri());

        try (ExecutorService executorProducts = Executors.newSingleThreadExecutor();
             ExecutorService executorClientRequests = Executors.newSingleThreadExecutor()) {
            executorProducts.execute(() -> {
                try {
                    log.info("Start consuming allowed-products");
                    moveProducts(fs);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            });

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

    private <T extends SpecificRecord> void writeToFile(
            FileSystem hdfs,
            ConsumerRecord<String, T> record
    ) throws IOException {
        String topic = record.topic();
        T value = record.value();

        String path = "/data/" + topic + "/" + System.currentTimeMillis() + ".avro";
        log.info("writeToFile\nrecord {}\nfile {}", record.value(), path);

        Path filePath = new Path(path);
        hdfs.mkdirs(filePath.getParent());

        Exception mainException = null;

        try (FSDataOutputStream out = hdfs.create(filePath, true)) {
            DatumWriter<T> writer = new SpecificDatumWriter<>(value.getSchema());
            try (DataFileWriter<T> dataWriter = new DataFileWriter<>(writer)) {
                dataWriter.create(value.getSchema(), out);
                dataWriter.append(value);
            } catch (Exception e) {
                mainException = e;
                throw e;
            }
        } catch (Exception e) {
            log.error("Failed to write record to HDFS at path {}: {}", filePath, e.getMessage(), e);
            if (mainException != null && mainException != e) {
                mainException.addSuppressed(e);
            }
            throw e;
        }
    }

}
