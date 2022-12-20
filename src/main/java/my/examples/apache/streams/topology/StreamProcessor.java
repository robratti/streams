package my.examples.apache.streams.topology;

import org.apache.kafka.streams.KafkaStreams;

public interface StreamProcessor {
    KafkaStreams getStream();
}
