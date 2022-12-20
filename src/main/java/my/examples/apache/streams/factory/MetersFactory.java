package my.examples.apache.streams.factory;

import io.micrometer.core.instrument.Meter;
import org.springframework.context.ApplicationContextAware;

import java.util.concurrent.CompletableFuture;

public interface MetersFactory <T extends Meter> extends ApplicationContextAware {
    CompletableFuture<T> getMeter(String meterName);
}