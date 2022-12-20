package my.examples.apache.streams.listener;

import io.micrometer.core.instrument.Counter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.examples.apache.streams.event.DlqEvent;
import my.examples.apache.streams.factory.CounterFactoryImpl;
import my.examples.apache.streams.factory.MetersFactory;
import my.examples.apache.streams.producer.DLQProducer;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class DlqEventListener implements ApplicationListener<DlqEvent> {
    private final DLQProducer dlqProducer;
    private final MetersFactory<Counter> counterMetersFactory;

    @Override
    public void onApplicationEvent(DlqEvent event) {
         dlqProducer.send(
                 String.valueOf(event.getDlqObject().getKey()),
                 event.getDlqObject()
            ).doOnSuccess(
                    unused -> counterMetersFactory.getMeter(CounterFactoryImpl.DLQ_PRODUCED
                    ).thenAccept(Counter::increment))
                 .subscribe();
    }
}