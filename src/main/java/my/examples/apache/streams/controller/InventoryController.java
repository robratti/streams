package my.examples.apache.streams.controller;

import lombok.AllArgsConstructor;
import my.examples.apache.streams.dtl.BatchRequest;
import my.examples.apache.streams.dtl.Sum;
import my.examples.apache.streams.producer.BatchProducer;
import my.examples.apache.streams.producer.OutboundAdapter;
import my.examples.apache.streams.service.InventoryService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@AllArgsConstructor
public class InventoryController {
    private final BatchProducer batchProducer;
    private final InventoryService inventoryService;
    private final OutboundAdapter<String, Integer> inventoryProducer;

    @PostMapping(value = "/new-entry", consumes = {"application/json"})
    public Mono<Void> newEntry(@RequestBody Sum sum) {
        return inventoryProducer.send(sum.getName(), sum.getQuantity());
    }

    @PostMapping(value = "/batch", consumes = {"application/json"})
    public Mono<Void> newEntry(@RequestBody BatchRequest request) {
        return batchProducer.produceInventory(new AtomicLong(request.getSize()));
    }

    @GetMapping(value = "/count/{name}", produces = {"application/json"})
    public Mono<Sum> getCount(@PathVariable String name) {
        return inventoryService.getCount(name);
    }

    @GetMapping(value = "/count", produces = {"application/json"})
    public Mono<Map<String, Integer>> getAll() {
        return inventoryService.getAll();
    }
}
