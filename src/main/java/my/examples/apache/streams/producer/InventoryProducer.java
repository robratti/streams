package my.examples.apache.streams.producer;

import my.examples.apache.streams.config.KafkaConfig;
import my.examples.apache.streams.dtl.Sum;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Component
public class InventoryProducer implements OutboundAdapter<String, Integer> {
    private final KafkaProducer<String, Sum<String, Integer>> kafkaProducer;

    public InventoryProducer(KafkaConfig kafkaConfig) {
        var configuration = kafkaConfig.getKafkaProperties();
        var jsonSerde = new JsonSerde<Sum<String, Integer>>();
        var jsonSerdeConfig = new HashMap<String, Object>();
        jsonSerdeConfig.put(JsonDeserializer.TRUSTED_PACKAGES, "my.examples.apache.streams.dtl");
        jsonSerde.configure(jsonSerdeConfig, false);
        configuration.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configuration.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, jsonSerde.serializer().getClass());
        this.kafkaProducer = new KafkaProducer<>(configuration);
    }

    @Override
    public Mono<Void> send(String key, Integer value) {
        return Mono.fromFuture(CompletableFuture.runAsync(() -> kafkaProducer.send(
                new ProducerRecord<>("inventory", key, new Sum<String, Integer>(key, value)))
        )).then();
    }
}
