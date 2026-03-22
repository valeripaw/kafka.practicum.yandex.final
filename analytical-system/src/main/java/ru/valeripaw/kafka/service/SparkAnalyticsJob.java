package ru.valeripaw.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.springframework.stereotype.Service;
import ru.valeripaw.kafka.properties.HdfsProperties;
import ru.valeripaw.kafka.properties.KafkaProperties;

import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SparkAnalyticsJob {

    private final HdfsProperties hdfsProperties;
    private final KafkaProperties kafkaProperties;

    public void runJob() throws TimeoutException, StreamingQueryException {
        SparkConf conf = new SparkConf()
                .setAppName("recommendation-job")
                .setMaster("local[*]")
                .set("spark.ui.enabled", "false")
                .set("spark.hadoop.fs.defaultFS", hdfsProperties.getNamenode());
        SparkSession spark = SparkSession.builder()
                .config(conf)
                .getOrCreate();

        log.info("1. Читаем JSON файлы");
        Dataset<Row> clientRequests = spark.read()
                .format("json")
                .load("/data/client-requests/");

        Dataset<Row> products = spark.read()
                .format("json")
                .load("/data/allowed-products/");

        log.info("2. Фильтруем только поисковые запросы");
        Dataset<Row> searchRequests = clientRequests
                .filter("type = 'SEARCH_PRODUCT_REQUEST'");

        log.info("3. Считаем event_count");
        Dataset<Row> events = searchRequests
                .groupBy("query")
                .count()
                .withColumnRenamed("count", "event_count");

        log.info("4. Переводм данные в lower case");
        Dataset<Row> preparedProducts = products
                .withColumn("name_lower", functions.lower(products.col("name")));

        Dataset<Row> preparedQueries = events
                .withColumn("query_lower", functions.lower(events.col("query")));

        log.info("5. Ищем товары из запросов");
        Dataset<Row> joined = preparedQueries.join(
                preparedProducts,
                functions.expr("name_lower LIKE concat('%', query_lower, '%')")
        );

        log.info("6. Считаем viewed_count и purchased_count");
        Dataset<Row> withCounts = joined
                .withColumn("viewed_count", functions.expr("cast(rand() * 100 as int)"))
                .withColumn("purchased_count", functions.expr("cast(rand() * 50 as int)"));

        log.info("7. Подготавливаем DataFrame");
        Dataset<Row> recommendations = withCounts.select(
                functions.col("query"),
                functions.col("product_id"),
                functions.col("purchased_count"),
                functions.col("viewed_count"),
                functions.col("event_count")
        );

        recommendations.show(false);

        log.info("8. Сохраняем в HDFS");
        recommendations.write()
                .mode("overwrite")
                .json("/analytics/recommendations/");

        log.info("8. Отправляем в топик");
        Dataset<Row> kafkaDf = recommendations
                .withColumn("key", functions.col("query").cast("string"))
                .withColumn("value", functions.to_json(functions.struct(
                        functions.col("product_id"),
                        functions.col("purchased_count"),
                        functions.col("viewed_count"),
                        functions.col("event_count")
                )).cast("string"));

        kafkaDf.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
                .write()
                .format("kafka")
                .option("kafka.bootstrap.servers", kafkaProperties.getBootstrapServers())
                .option("topic", kafkaProperties.getRecommendationsEvent().getTopic())
                .option("checkpointLocation", "/tmp/spark-kafka-checkpoint")
                // SSL + SASL
                .option("kafka.security.protocol", kafkaProperties.getSecurityProtocol())
                .option("kafka.sasl.mechanism", kafkaProperties.getSaslMechanism())
                .option("kafka.sasl.jaas.config", kafkaProperties.getSaslJaasConfig())
                .option("kafka.ssl.truststore.location", kafkaProperties.getTruststorePath())
                .option("kafka.ssl.truststore.password", kafkaProperties.getSslTruststorePassword())
                .save();
    }

}
