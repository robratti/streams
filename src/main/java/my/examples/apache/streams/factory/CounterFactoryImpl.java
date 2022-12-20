package my.examples.apache.streams.factory;

import io.micrometer.core.instrument.Counter;
import org.springframework.stereotype.Service;

@Service
public class CounterFactoryImpl extends MetersFactoryImpl<Counter> {
    // Todo: adds Counter Names as follow example
    public static final String UPSTREAM_CONSUMED = "upstreamReceived";
    public static final String DOWNSTREAM_PRODUCED = "downstreamProduced";
    public static final String PROCESSED_METER = "processedMeter";
    public static final String DLQ_PRODUCED = "dlqProduced";

    public CounterFactoryImpl() {
        super(Counter.class);
    }
}