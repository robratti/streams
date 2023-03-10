# Kafka Streams

## Create Topics
```
kafka-topics --bootstrap-server broker-service:9092 --create --topic inventory --replication-factor 1 --partitions 3
kafka-topics --bootstrap-server broker-service:9092 --create --topic order --replication-factor 1 --partitions 3
kafka-topics --bootstrap-server broker-service:9092 --create --topic aggregated-inventory --replication-factor 1 --partitions 1
kafka-topics --bootstrap-server broker-service:9092 --list
```


## Change topic to compact
```
kafka-configs --bootstrap-server broker-service:9092 --entity-type topics --entity-name aggregated-inventory --alter --add-config cleanup.policy=compact
kafka-topics --bootstrap-server broker-service:9092 --describe --topic aggregated-inventory
```


## Change retention policies
```
kafka-configs --bootstrap-server broker-service:9092 --entity-type brokers --entity-name 1 --alter --add-config-file ./kafka.config
```


## Groups
### List All Groups
```
kafka-consumer-groups --bootstrap-server broker-service:9092 --list --all-groups
```

### Reset Offsets for group
```
kafka-consumer-groups --bootstrap-server broker-service:9092 --reset-offsets --group stream-table --topic inventory --to-earliest --dry-run
kafka-consumer-groups --bootstrap-server broker-service:9092 --reset-offsets --group stream-table --topic inventory --to-earliest --execute
```
