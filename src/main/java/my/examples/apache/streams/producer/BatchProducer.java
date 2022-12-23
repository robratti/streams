package my.examples.apache.streams.producer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@AllArgsConstructor
public class BatchProducer {
    private final InventoryProducer inventoryProducer;
    private final Logger logger = LoggerFactory.getLogger(BatchProducer.class.getName());

    private static final String[] ITEMS = {"apples", "oranges", "avocados", "mangos", "bananas", "strawberries"};

    public Mono<Void> produceInventory(AtomicLong size) {
        return Mono.fromFuture(CompletableFuture.runAsync(() -> {
            while (size.get() > 0) {
                try {
                    var item = ITEMS[ThreadLocalRandom.current().nextInt(0, ITEMS.length-1)];
                    var quantity = ThreadLocalRandom.current().nextInt(-1, 100);

                    inventoryProducer.send(item, quantity).subscribe();
                    size.decrementAndGet();
                    Thread.sleep(ThreadLocalRandom.current().nextLong(0, 1000));
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
            }
            logger.info(String.format("Finished seeding inventory with %s items", size));
        }));
    }
}
