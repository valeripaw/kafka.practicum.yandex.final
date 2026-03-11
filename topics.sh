#!/bin/bash

docker exec kafka1-1 kafka-topics \
  --create \
  --topic products \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --partitions 1 \
  --replication-factor 1

docker exec kafka1-1 kafka-acls \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:producer \
  --operation WRITE \
  --operation READ \
  --topic products

docker exec kafka1-1 kafka-topics \
  --create \
  --topic allowed-products \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --partitions 1 \
  --replication-factor 1

docker exec kafka1-1 kafka-acls \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:producer \
  --operation WRITE \
  --topic allowed-products

docker exec kafka1-1 kafka-topics \
  --create \
  --topic client-requests \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --partitions 1 \
  --replication-factor 1

docker exec kafka1-1 kafka-acls \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:producer \
  --operation WRITE \
  --topic client-requests

docker exec kafka1-1 kafka-acls \
  --bootstrap-server localhost:9092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:producer \
  --operation READ \
  --group shop-api-service

# kafka connect

docker exec kafka1-1 kafka-acls \
  --bootstrap-server localhost:9092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:kafkaconnect \
  --operation READ \
  --operation WRITE \
  --operation CREATE \
  --operation DESCRIBE \
  --topic connect-configs

docker exec kafka1-1 kafka-acls \
  --bootstrap-server localhost:9092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:kafkaconnect \
  --operation READ \
  --operation WRITE \
  --operation CREATE \
  --operation DESCRIBE \
  --topic connect-offsets

docker exec kafka1-1 kafka-acls \
  --bootstrap-server localhost:9092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:kafkaconnect \
  --operation READ \
  --operation WRITE \
  --operation CREATE \
  --operation DESCRIBE \
  --topic connect-status

docker exec kafka1-1 kafka-acls \
  --bootstrap-server localhost:9092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:kafkaconnect \
  --operation READ \
  --group kafka-connect

docker exec kafka1-1 kafka-acls \
  --bootstrap-server localhost:9092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:kafkaconnect \
  --operation READ \
  --group connect-allowed-products-postgres-sink

docker exec kafka1-1 kafka-acls \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:kafkaconnect \
  --operation READ \
  --operation DESCRIBE \
  --topic allowed-products
