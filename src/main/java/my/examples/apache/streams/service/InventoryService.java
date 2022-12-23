package my.examples.apache.streams.service;

import jakarta.annotation.PostConstruct;
import my.examples.apache.streams.config.KafkaConfig;
import my.examples.apache.streams.dtl.Sum;
import my.examples.apache.streams.topology.StreamProcessorImpl;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Service
public class InventoryService {
    private final KafkaConfig kafkaConfig;
    private ReadOnlyKeyValueStore<String, Sum> inventoryStore;

    public InventoryService(KafkaConfig kafkaConfig) {
        this.kafkaConfig = kafkaConfig;
    }

    @PostConstruct
    private void initializeStore() {
        var stream = new StreamProcessorImpl(kafkaConfig).getStream();
        StoreQueryParameters<ReadOnlyKeyValueStore<String, Sum>> storeQueryParameters = StoreQueryParameters.fromNameAndType("inventory-store", QueryableStoreTypes.keyValueStore());
        stream.start();
        this.inventoryStore = stream.store(
            storeQueryParameters
        );
    }

    public Mono<Sum> getCount(String name) {
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> inventoryStore.get(name)));
    }
}
