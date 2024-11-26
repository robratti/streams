package my.examples.apache.streams.topology;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.examples.apache.streams.config.KafkaConfig;
import my.examples.apache.streams.config.properties.BatchesConfig;
import my.examples.apache.streams.dtl.Sum;
import my.examples.apache.streams.producer.BatchProducer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.HashMap;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class StreamProcessorImpl implements StreamProcessor {
    private final KafkaConfig kafkaConfig;
    private final BatchesConfig batches;
    private final boolean enableRandomDelay;
    private final Logger logger = LoggerFactory.getLogger(BatchProducer.class.getName());

    @Override
    public KafkaStreams getStream() {
        var streamBuilder = new StreamsBuilder();

        buildStream(streamBuilder);

        return new KafkaStreams(streamBuilder.build(), kafkaConfig.getKafkaProperties());
    }

    public JsonSerde<Sum<String, Integer>> getValueSerde() {
        var jsonSerde = new JsonSerde<Sum<String, Integer>>();
        var jsonSerdeConfig = new HashMap<String, Object>();
        jsonSerdeConfig.put(JsonDeserializer.TRUSTED_PACKAGES, "my.examples.apache.streams.dtl");
        jsonSerdeConfig.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Sum.class.getName());
        jsonSerde.configure(jsonSerdeConfig, false);

        return jsonSerde;
    }

    public StreamsBuilder buildStream(StreamsBuilder streamBuilder) {
        var jsonSerde = getValueSerde();

        // 1. Aggregate inventory
        streamBuilder.stream("inventory", Consumed.with(Serdes.String(), jsonSerde))
                .groupByKey()
                .aggregate(Sum::new, (s, sum, sum2) -> {
                    var partial = new Sum<String, Integer>(null, 0);
                    partial.setName(s);
                    var q1 = Optional.ofNullable(sum.getQuantity()).orElse(0);
                    var q2 = Optional.ofNullable(sum2.getQuantity()).orElse(0);
                    partial.setQuantity(q1+q2);
                    logger.info(String.format("New event consumed, produced sub-total of %s", partial));

                    return partial;
                }, Materialized.<String, Sum<String, Integer>, KeyValueStore<Bytes, byte[]>>as("inventory-store")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(jsonSerde))
                .toStream()
                .to("aggregated-inventory");

        // 2. Generate orders for negative inventory
        streamBuilder.stream("aggregated-inventory", Consumed.with(Serdes.String(), jsonSerde))
                .filter((key, value) -> value.getQuantity() < 0)
                .mapValues(value -> {
                    String itemName = value.getName();
                    Integer requiredBatch = batches.getItems().getOrDefault(itemName, 1);
                    int deficit = Math.abs(value.getQuantity());
                    int minimumOrderQuantity = ((deficit + requiredBatch - 1) / requiredBatch) * requiredBatch;
                    var order = new Sum<String, Integer>();
                    order.setName(itemName);
                    order.setQuantity(minimumOrderQuantity);
                    logger.info(String.format("Shortage of %s detected, creating order: %s", itemName, order));
                    return order;
                })
                .to("order", Produced.with(Serdes.String(), jsonSerde));

        streamBuilder.stream("order", Consumed.with(Serdes.String(), jsonSerde))
                .peek((key, value) -> {
                    if (enableRandomDelay) {
                        try {
                            // Simulate random processing delay (100ms to 500ms)
                            long delay = (long) (100 + Math.random() * 400);
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            logger.error("Error during simulated delay", e);
                            Thread.currentThread().interrupt();
                        }
                    }
                })
                .to("inventory", Produced.with(Serdes.String(), jsonSerde));

        return streamBuilder;
    }
}
