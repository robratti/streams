package my.examples.apache.streams.topology;

import my.examples.apache.streams.config.KafkaConfig;
import my.examples.apache.streams.dtl.Sum;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreamProcessorImplTest {
    @Mock
    Environment environment;
    @Test
    void getStream() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"local"});
        when(environment.getProperty("spring.kafka.properties.bootstrap.servers")).thenReturn("dummy:1234");
        when(environment.getProperty("spring.kafka.properties.application-id")).thenReturn("stream-test-application");
        when(environment.getProperty("spring.kafka.properties.schema.registry.url")).thenReturn("mock://" + this.getClass().getName());
        var streamBuilder = new StreamsBuilder();
        KafkaConfig kafkaConfig = new KafkaConfig(environment);
        var streamProcessor = new StreamProcessorImpl(kafkaConfig);
        streamProcessor.buildStream(streamBuilder);

        //Serdes
        var keySerde = Serdes.String();
        var valueSerde = streamProcessor.getValueSerde();

        //Create Test Driver
        var testDriver = new TopologyTestDriver(streamBuilder.build(), kafkaConfig.getKafkaProperties());

        testDriver.createInputTopic(
                "inventory",
                keySerde.serializer(),
                valueSerde.serializer()
        ).pipeKeyValueList(Arrays.asList(
                new KeyValue<>("apples", new Sum<>("apples", 10)),
                new KeyValue<>("apples", new Sum<>("apples", 20))
        ));

        var testOutputTopicForLocationInfo = testDriver.createOutputTopic(
                "aggregated-inventory",
                keySerde.deserializer(),
                valueSerde.deserializer()
        );

        var result = testOutputTopicForLocationInfo.readKeyValuesToList();

        assertEquals(2, result.size(), "Asserting 2 logs are produced downstream.");
        assertEquals(10, result.get(0).value.getQuantity(), "Asserting the first log sum is 10.");
        assertEquals(30, result.get(1).value.getQuantity(), "Asserting the second log sum is 30.");

        testDriver.close();
    }
}