package my.examples.apache.streams.service;

import jakarta.annotation.PostConstruct;
import my.examples.apache.streams.config.KafkaConfig;
import my.examples.apache.streams.config.properties.BatchesConfig;
import my.examples.apache.streams.dtl.Sum;
import my.examples.apache.streams.topology.StreamProcessorImpl;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class InventoryService {
    private final KafkaConfig kafkaConfig;
    private final BatchesConfig batchesConfig;
    private ReadOnlyKeyValueStore<String, Sum<String, Integer>> inventoryStore;

    public InventoryService(KafkaConfig kafkaConfig, BatchesConfig batchesConfig) {
        this.kafkaConfig = kafkaConfig;
        this.batchesConfig = batchesConfig;
    }

    @PostConstruct
    private void initializeStore() {
        var stream = new StreamProcessorImpl(kafkaConfig, batchesConfig, true).getStream();
        StoreQueryParameters<ReadOnlyKeyValueStore<String, Sum<String, Integer>>> storeQueryParameters =
                StoreQueryParameters.fromNameAndType("inventory-store", QueryableStoreTypes.keyValueStore());
        stream.start();
        this.inventoryStore = stream.store(
            storeQueryParameters
        );
    }

    public Mono<Sum<String, Integer>> getCount(String name) {
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> inventoryStore.get(name)));
    }

    public Mono<Map<String, Integer>> getAll() {
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            var result = inventoryStore.all();
            var mapResult = new HashMap<String, Integer>();
            while (result.hasNext()) {
                var record = result.next();
                mapResult.put(record.key, record.value.getQuantity());
            }
            result.close();

            return mapResult;
        }));
    }
}
