package my.examples.apache.streams.topology;

import my.examples.apache.streams.config.KafkaConfig;
import my.examples.apache.streams.config.properties.BatchesConfig;
import my.examples.apache.streams.dtl.Sum;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreamProcessorImplTest {

    @Mock
    Environment environment;
    @Mock
    BatchesConfig batchesConfig;

    private TopologyTestDriver testDriver;
    private StreamProcessorImpl streamProcessor;

    @BeforeEach
    void setup() {
        // Mock environment properties
        when(environment.getActiveProfiles()).thenReturn(new String[]{"local"});
        when(environment.getProperty("spring.kafka.properties.bootstrap.servers")).thenReturn("dummy:1234");
        when(environment.getProperty("spring.kafka.properties.application-id")).thenReturn("stream-test-application");
        when(environment.getProperty("spring.kafka.properties.schema.registry.url")).thenReturn("mock://" + this.getClass().getName());

        // Mock batch configuration
        when(batchesConfig.getItems()).thenReturn(Map.of("apples", 50));

        // Initialize components
        StreamsBuilder streamBuilder = new StreamsBuilder();
        KafkaConfig kafkaConfig = new KafkaConfig(environment);
        streamProcessor = new StreamProcessorImpl(kafkaConfig, batchesConfig, false);
        streamProcessor.buildStream(streamBuilder);

        // Initialize Test Driver
        testDriver = new TopologyTestDriver(streamBuilder.build(), kafkaConfig.getKafkaProperties());
    }

    @AfterEach
    void teardown() {
        testDriver.close();
    }

    @Test
    void testUpdatedTopology() {
        // Simulate Input
        testDriver.createInputTopic("inventory", Serdes.String().serializer(), streamProcessor.getValueSerde().serializer())
                .pipeKeyValueList(Arrays.asList(
                        new KeyValue<>("apples", new Sum<>("apples", 10)),
                        new KeyValue<>("apples", new Sum<>("apples", 20)),
                        new KeyValue<>("apples", new Sum<>("apples", -40))
                ));

        // Check Aggregated Inventory
        var aggregatedInventoryOutputTopic = testDriver.createOutputTopic(
                "aggregated-inventory",
                Serdes.String().deserializer(),
                streamProcessor.getValueSerde().deserializer()
        );

        var aggregatedResults = aggregatedInventoryOutputTopic.readKeyValuesToList();
        assertEquals(4, aggregatedResults.size(), "Asserting 2 aggregated logs are produced.");
        assertEquals(10, aggregatedResults.get(0).value.getQuantity(), "Asserting the first log sum is 10.");
        assertEquals(30, aggregatedResults.get(1).value.getQuantity(), "Asserting the second log sum is 30.");
        assertEquals(-10, aggregatedResults.get(2).value.getQuantity(), "Asserting the second log sum is -10.");
        assertEquals(40, aggregatedResults.get(3).value.getQuantity(), "Asserting the second log sum is 40.");
    }
}

