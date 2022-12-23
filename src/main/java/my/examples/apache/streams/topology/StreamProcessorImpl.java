package my.examples.apache.streams.topology;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.examples.apache.streams.config.KafkaConfig;
import my.examples.apache.streams.dtl.Sum;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.HashMap;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class StreamProcessorImpl implements StreamProcessor {
    private final KafkaConfig kafkaConfig;

    @Override
    public KafkaStreams getStream() {
        var streamBuilder = new StreamsBuilder();

        buildStream(streamBuilder);

        return new KafkaStreams(streamBuilder.build(), kafkaConfig.getKafkaProperties());
    }

    public JsonSerde<Sum> getValueSerde() {
        var jsonSerde = new JsonSerde<Sum>();
        var jsonSerdeConfig = new HashMap<String, Object>();
        jsonSerdeConfig.put(JsonDeserializer.TRUSTED_PACKAGES, "my.examples.apache.streams.dtl");
        jsonSerdeConfig.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Sum.class.getName());
        jsonSerde.configure(jsonSerdeConfig, false);

        return jsonSerde;
    }

    public StreamsBuilder buildStream(StreamsBuilder streamBuilder) {
        var jsonSerde = getValueSerde();

        streamBuilder.stream("inventory", Consumed.with(Serdes.String(), jsonSerde))
                .groupByKey()
                .aggregate(Sum::new, (s, sum, sum2) -> {
                    var partial = new Sum(null, 0);
                    partial.setName(s);
                    var q1 = Optional.ofNullable(sum.getQuantity()).orElse(0);
                    var q2 = Optional.ofNullable(sum2.getQuantity()).orElse(0);
                    partial.setQuantity(q1+q2);

                    return partial;
                }, Materialized.<String, Sum, KeyValueStore<Bytes, byte[]>>as("inventory-store")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(jsonSerde))
                .toStream()
                .to("aggregated-inventory");

        return streamBuilder;
    }
}
