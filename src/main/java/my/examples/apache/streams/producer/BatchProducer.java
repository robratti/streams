package my.examples.apache.streams.producer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.examples.apache.streams.dtl.Sum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@AllArgsConstructor
public class BatchProducer {
    private final InventoryProducer inventoryProducer;
    private final Logger logger = LoggerFactory.getLogger(BatchProducer.class.getName());

    private static final String[] ITEMS = {"apples", "oranges", "avocados", "mangos", "bananas", "strawberries"};

    public Flux<Sum<String, Integer>> produceInventory(AtomicLong size) {
        var requestArray = new ArrayList<Sum<String, Integer>>();
        while (size.get() > 0) {
            var item = ITEMS[ThreadLocalRandom.current().nextInt(0, ITEMS.length-1)];
            var quantity = ThreadLocalRandom.current().nextInt(-100, 100);
            requestArray.add(new Sum<>(item, quantity));
            size.decrementAndGet();
        }

        return Flux.fromIterable(requestArray)
                .map(sum -> Tuples.of(sum, inventoryProducer.send(sum.getName(), sum.getQuantity())))
                .flatMap(tuple -> tuple.getT2().thenReturn(tuple.getT1()))
                .doOnComplete(() -> logger.info(String.format("Finished seeding inventory with %s items", size)))
                .delayElements(Duration.ofMillis(ThreadLocalRandom.current().nextLong(1000, 1500)));

    }
}
