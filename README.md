# Запуск Kafka-кластера

Запустите kafka кластер командой:
```
docker-compose -f docker-compose-kafka.yml up -d
```

Подождите 1–2 минуты, пока все сервисы запустятся.

Будут созданы два кафка кластера: условно №1 и №2.             
Кластер №1 - кластер, к которому будут подключаться `SHOP API` и `CLIENT API`.

# Топик

Создаем топик `products` на кластере №1:
```
docker exec kafka1-1 kafka-topics \
  --create \
  --topic products \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --partitions 2 \
  --replication-factor 2
```

Выдаем права продьюсеру:
```
docker exec kafka1-1 kafka-acls \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:producer \
  --operation WRITE \
  --topic products
```

# Schema Registry

Схема [product.avsc](common/src/main/avro/product.avsc).

Регистрируем схему на кластере №1:

```
curl -X POST http://localhost:8081/subjects/products-value/versions \
-H "Content-Type: application/vnd.schemaregistry.v1+json" \
-d '{
  "schema": "{\"type\": \"record\", \"name\": \"ProductAvro\", \"namespace\": \"ru.valeripaw.kafka.dto\", \"version\": \"1\", \"fields\": [{\"name\": \"product_id\", \"type\": \"string\"}, {\"name\": \"name\", \"type\": \"string\"}, {\"name\": \"description\", \"type\": [\"null\", \"string\"], \"default\": null}, {\"name\": \"price\", \"type\": {\"name\": \"PriceAvro\", \"type\": \"record\", \"fields\": [{\"name\": \"amount\", \"type\": [\"null\", \"double\"], \"default\": null}, {\"name\": \"currency\", \"type\": [\"null\", \"string\"], \"default\": null}]}}, {\"name\": \"category\", \"type\": \"string\"}, {\"name\": \"brand\", \"type\": \"string\"}, {\"name\": \"stock\", \"type\": {\"name\": \"StockAvro\", \"type\": \"record\", \"fields\": [{\"name\": \"available\", \"type\": [\"null\", \"int\"], \"default\": null}, {\"name\": \"reserved\", \"type\": [\"null\", \"int\"], \"default\": null}]}}, {\"name\": \"sku\", \"type\": \"string\"}, {\"name\": \"specifications\", \"type\": [\"null\", {\"name\": \"SpecificationsAvro\", \"type\": \"record\", \"fields\": [{\"name\": \"weight\", \"type\": [\"null\", \"string\"], \"default\": null}, {\"name\": \"dimensions\", \"type\": [\"null\", \"string\"], \"default\": null}, {\"name\": \"battery_life\", \"type\": [\"null\", \"string\"], \"default\": null}, {\"name\": \"water_resistance\", \"type\": [\"null\", \"string\"], \"default\": null}]}], \"default\": null}, {\"name\": \"created_at\", \"type\": \"string\"}, {\"name\": \"updated_at\", \"type\": \"string\"}, {\"name\": \"index\", \"type\": \"string\"}, {\"name\": \"store_id\", \"type\": \"string\"}, {\"name\": \"tags\", \"default\": null, \"type\": [\"null\", {\"type\": \"array\", \"items\": \"string\"}]}, {\"name\": \"images\", \"default\": null, \"type\": [\"null\", {\"type\": \"array\", \"items\": {\"name\": \"ImageAvro\", \"type\": \"record\", \"fields\": [{\"name\": \"url\", \"type\": \"string\"}, {\"name\": \"alt\", \"type\": [\"null\", \"string\"], \"default\": null}]}}]}]}"
}'
```

В ответе будет что-то похожее на:
```json
{
  "id": 1,
  "version": 1,
  "guid": "2ad0f59e-dd54-286e-275b-1d425f722d85",
  "schemaType": "AVRO",
  "schema": "<schema>"
}
```

# SHOP API

```
products.json
    │
    ▼
File Watcher
    │
    ▼
ProductEventService
    │
    ▼
Kafka Producer
    │
    ▼
Kafka topic: products
```

Приложение слушает изменения в файле `products.json` и смотрит на значение в `updated_at` у каждого продукта: если оно не изменилось по сравнению с предыдущей версией, сообщение в кафку отправлено не будет.

# CLIENT API

todo

## Запуск

Запустите kafka кластер командой:
```
docker-compose -f docker-compose-shop-api.yml up -d
```

Подождите 1–2 минуты, пока все сервисы запустятся.

# Остановка кластера

1. Остановите кластер командой:

```
docker-compose down
```

2. Для полной очистки (включая данные) можно использовать команду:

```
docker-compose down -v
```
