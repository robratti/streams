# Kafka Streams

# Local Infrastructure
## Requirements
* Docker
* Kubernetes
* Java Virtual Machine > 16

## Run Kafka 1 Broker Cluster
1. From the _source folder_ execute `kubectl apply -k ./kubernetes/kafka` for running the local infrastructure.
2. Forward broker-service port `9092` to localhost `kubectl port-forward --namespace kafka service/broker-service 9092:9092`
3. change configuration to port 9092 `spring.kafka.properties.bootstrap.servers`

## Run Kafka 3 Brokers Cluster
1. From the _source folder_ execute `kubectl apply -k ./kubernetes/k_multinode` for running the local infrastructure.
2. Forward broker-service port `29092` to localhost `kubectl port-forward --namespace kafka service/broker1-service 29092:29092`
3. change configuration to port 9092 `spring.kafka.properties.bootstrap.servers`

Have a look at [Help.md](HELP.md) for creating topics and other settings

Execute `mvn install` and then `mvn spring-boot:run`
