package ru.valeripaw.kafka.service;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Service;

/**
 * Читает данные из HDFS и делает аналитику.
 */
//@Service
public class SparkAnalyticsJob {

    public void runJob() {
        SparkSession spark = SparkSession
                .builder()
                .appName("recommendation-job")
                .master("local[*]")
                .getOrCreate();

        Dataset<Row> requests =
                spark.read()
                        .format("avro")
                        .load("/data/client-requests/");

        Dataset<Row> products =
                spark.read()
                        .format("avro")
                        .load("/data/allowed-products/");

        // события
        Dataset<Row> views =
                requests.filter("type = 'view'");

        Dataset<Row> purchases =
                requests.filter("type = 'purchase'");

        Dataset<Row> viewedStats =
                views.groupBy("query")
                        .count()
                        .withColumnRenamed("count", "viewed_count");

        Dataset<Row> purchaseStats =
                purchases.groupBy("query")
                        .count()
                        .withColumnRenamed("count", "purchased_count");

        Dataset<Row> events =
                requests.groupBy("query")
                        .count()
                        .withColumnRenamed("count", "event_count");

        Dataset<Row> result =
                viewedStats
                        .join(purchaseStats, "query")
                        .join(events, "query");

        result.show();

        result.write()
                .format("json")
                .save("/analytics/recommendations/");
    }

}
