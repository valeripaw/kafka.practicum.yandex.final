# План развертывания
1. Запуск кластера (zookeeper, два кафка брокера, postgres, kafka-ui).
2. Создание топиков и раздача прав на них и на группы.
3. Регистрация схем в `Schema Registry`.
4. Запуск `kafka connect`.
5. Запуск `SHOP API` и `CLIENT API`.

# Запуск кластера

Запустите кластер командой:

```shell
docker-compose -f docker-compose-cluster.yml up -d
```

Подождите 1–2 минуты, пока все сервисы запустятся.

Будут созданы:
- два кафка кластера: условно №1 и №2.             
Кластер №1 - кластер, к которому будут подключаться `SHOP API` и `CLIENT API`.
- postgres с двумя базами `shop_db` и `client_db`. 
  В базе `shop_db` будет создана таблица `product`.
  В базе `client_db` будет создана таблица `client_request`.

# Топики и группы

- `products` - топик, куда будут попадать все продукты из файла `products.json` (`SHOP API`);
- `allowed-products` - топик, куда будут попадать только разрешенные продукты (`SHOP API`);
- `client-requests` - топик для клиентских запросов (`CLIENT API`).

Минимальное количество реплик для отказоустойчивости в двух брокерах (у меня в каждом кластере настроено именно столько) - максимум 2. 
Для синхронности нужно установить `min.insync.replicas = 2` на брокерах или `replication-factor = 2` в топиках. 
Но из-за ограничений ноута, при таких настройках у меня всё время что-то отваливается.
Поэтому, что в docker-compose файле, что далее в топиках стоит значение 1.

## Команды
Все следующие команды собраны в файл [topics.sh](topics.sh). И можно не выполнять команды по одной, а запустить скрипт:
```shell
bash topics.sh
```

<details>
<summary>Описание команд (раскрыть)</summary>

Создаем топик `products` на кластере №1:
```
docker exec kafka1-1 kafka-topics \
  --create \
  --topic products \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --partitions 1 \
  --replication-factor 1
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

Создаем топик `allowed-products` на кластере №1:
```
docker exec kafka1-1 kafka-topics \
  --create \
  --topic allowed-products \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --partitions 1 \
  --replication-factor 1
```

Выдаем права продьюсеру:
```
docker exec kafka1-1 kafka-acls \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:producer \
  --operation WRITE \
  --topic allowed-products
```

Создаем топик `client-requests` на кластере №1:
```
docker exec kafka1-1 kafka-topics \
  --create \
  --topic client-requests \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --partitions 1 \
  --replication-factor 1
```

Выдаем права продьюсеру:
```
docker exec kafka1-1 kafka-acls \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:producer \
  --operation WRITE \
  --topic client-requests
```

### Права для Kafka Streams
Kafka Streams нужны права на группу `shop-api-service`, на чтение `products`, на запись в `allowed-products`.
Так как я везде использую `User:producer`, то на запись в `allowed-products` права уже выданы выше. Выдаем остальные.
На группу:
```
docker exec kafka1-1 kafka-acls \
  --bootstrap-server localhost:9092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:producer \
  --operation READ \
  --group shop-api-service
```
где `shop-api-service` задается в конфиге `SHOP API` в `kafka.streams-application-id`.

На чтение `products`:
```
docker exec kafka1-1 kafka-acls \
  --bootstrap-server localhost:9092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:producer \
  --operation READ \
  --topic products
```

### Права для Kafka Connect
Kafka Connect использует 3 служебных топика:
- connect-configs
- connect-offsets
- connect-status

И пользователь должен иметь права:
- READ
- WRITE
- CREATE
- DESCRIBE

```
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
```

А так же права на группы `kafka-connect` и `connect-allowed-products-postgres-sink`:
```
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
```

И права на топик `allowed-products`:
```
docker exec kafka1-1 kafka-acls \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:kafkaconnect \
  --operation READ \
  --operation DESCRIBE \
  --topic allowed-products
```

</details>

# Schema Registry

Схема [product.avsc](common/src/main/avro/product.avsc).           
Схема [client.request.avsc](common%2Fsrc%2Fmain%2Favro%2Fclient.request.avsc).

Регистрируем схему для топика `products` на кластере №1:
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

Регистрируем схему для топика `client-requests` на кластере №1:
```
curl -X POST http://localhost:8081/subjects/client-requests-value/versions \
-H "Content-Type: application/vnd.schemaregistry.v1+json" \
-d '{
  "schema": "{\"type\": \"record\", \"name\": \"ClientRequestAvro\", \"namespace\": \"ru.valeripaw.kafka.dto\", \"version\": \"1\", \"fields\": [{\"name\": \"type\", \"type\": \"string\"}, {\"name\": \"query\", \"type\": \"string\"}, {\"name\": \"created_at\", \"type\": \"long\"}]}"
}'
```

В ответе будет что-то похожее на:
```json
{
  "id": 2,
  "version": 1,
  "guid": "14e507f2-a281-f8bc-42a7-11cc836ffcf4",
  "schemaType": "AVRO",
  "schema": "<schema>"
}
```

Пример сообщения для топика `client-requests`:
```json
{
  "type": "SEARCH_PRODUCT_REQUEST",
  "query": "Умные часы",
  "created_at": 1719991111
}
```
- `"type": "SEARCH_PRODUCT_REQUEST"` - для поиска, для рекомендаций - `"type": "RECOMMENDATION_REQUEST"`.

# Kafka Connect
Запустите кластер командой:

```shell
docker-compose -f docker-compose-kafka-konnect.yml up -d
```

Подождите 1–2 минуты, пока все сервисы запустятся.

Конфиг [config.json](shop-api/kafka-connect/config.json).

Таблица `product` уже создана.

Создание коннектора:
```
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  --data-binary "@shop-api/kafka-connect/config.json" 
```

Проверить статус:
```
curl http://localhost:8083/connectors/allowed-products-postgres-sink/status
```

В ответе должно быть что-то похожее на:
```json
{
  "name": "allowed-products-postgres-sink",
  "connector": {
    "state": "RUNNING",
    "worker_id": "kafka-connect:8083"
  },
  "tasks": [
    {
      "id": 0,
      "state": "RUNNING",
      "worker_id": "kafka-connect:8083"
    }
  ],
  "type": "sink"
}
```

Если по какой-то причине коннектор нужно удалить:
```
curl -X DELETE http://localhost:8083/connectors/allowed-products-postgres-sink
```

# SHOP API
Запустите `SHOP API` командой:

```shell
docker-compose -f docker-compose-shop-api.yml up -d
```

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
    │
    ▼
Kafka Streams
    │
    ▼
filter banned products
    │
    ▼
Kafka topic: allowed-products
    │
    ▼
Kafka Connect JDBC Sink
    │
    ▼
Postgres: product table
```

Приложение слушает изменения в файле `products.json`.                     
Все продукты из файла `products.json` попадают в топик `products`. Далее продукты из этого топика проходят фильтрацию на
запрещенные и попадают в топик `allowed-products`, и уже из топика `allowed-products` записываются в бд в таблицу `product`.

# CLIENT API
Запустите `CLIENT API` командой:

```shell
docker-compose -f docker-compose-client-api.yml up -d
```

```
REST or CLI
    │
    ▼
Postgres: client_request table
    │
    ▼
Kafka topic: client-requests
```

Приложение реализует команды:
- поиск информации о товаре по его имени
- получение персонализированных рекомендаций
  двумя способами: через REST и через терминал (использовать терминал в контейнерах не очень удобно, нужно конфигурировать отдельно, поэтому продублировала по ресту).

По REST можно отправить запросы:
поиск информации о товаре по его имени
```
curl --get \
  --data-urlencode "name=Планшет TabMax" \
  http://localhost:9197/api/product/search
```

получение персонализированных рекомендаций
```
curl --get \
  --data-urlencode "category=Электроника" \
  http://localhost:9197/api/product/recommendation
```


Запросы сохраняются в кластере №1 в топик `client-requests`, а так же в бд `client_db` в таблицу `client_request`.
Продукты в ответе берутся из бд `shop_db` из таблицы `product`.

# MirrorMaker
Запустите `MirrorMaker` командой:

```shell
docker-compose -f docker-compose-mirror-maker.yml up -d
```

На кластере №2 будут созданы топики `products`, `allowed-products`, `client-requests` со всем их содержимым как в кластере №1.             
А так же несколько служебных: `mm2-status.source.internal`, `mm2-offsets.source.internal`, `mm2-configs.source.internal`, `heartbeats`.          
Все права `ACL` так же будут перенесены.

Если `MirrorMaker` оставить запущеным, он почти в реальном времени будет переносить данные с кластера №1 на кластер №2.

# Остановка кластера

1. Остановите кластер командой:

```shell
docker-compose down
```

2. Для полной очистки (включая данные) можно использовать команду:

```shell
docker-compose down -v
```
