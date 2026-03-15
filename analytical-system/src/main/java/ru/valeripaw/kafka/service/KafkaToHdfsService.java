package ru.valeripaw.kafka.service;

import lombok.RequiredArgsConstructor;
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

import java.io.IOException;
import java.time.Duration;

/**
 * Читает топики и пишет Avro в HDFS.
 */
@Service
@RequiredArgsConstructor
public class KafkaToHdfsService {

    private final KafkaConsumer<String, ProductAvro> productConsumer;
    private final KafkaConsumer<String, ClientRequestAvro> clientRequestConsumer;

    public void moveData() throws IOException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);

        // todo запустить в потоках, чтобы одновременно
        moveProducts(fs);
        moveClientRequests(fs);
    }

    private void moveClientRequests(FileSystem fs) throws IOException {
        while (true) {
            ConsumerRecords<String, ClientRequestAvro> records = clientRequestConsumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, ClientRequestAvro> record : records) {
                writeToFile(fs, record);
            }
        }
    }

    private void moveProducts(FileSystem fs) throws IOException {
        while (true) {
            ConsumerRecords<String, ProductAvro> records = productConsumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, ProductAvro> record : records) {
                writeToFile(fs, record);
            }
        }
    }

    private <T extends SpecificRecord> void writeToFile(FileSystem fs, ConsumerRecord<String, T> record) throws IOException {
        String topic = record.topic();
        T value = record.value();

        String path = "/data/" + topic + "/" + System.currentTimeMillis() + ".avro";
        FSDataOutputStream out = fs.create(new Path(path));

        DatumWriter<T> writer = new SpecificDatumWriter<>(value.getSchema());
        DataFileWriter<T> dataWriter = new DataFileWriter<>(writer);

        dataWriter.create(value.getSchema(), out);
        dataWriter.append(value);

        dataWriter.close();
    }

}
