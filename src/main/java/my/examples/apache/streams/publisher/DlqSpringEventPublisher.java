package my.examples.apache.streams.publisher;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.examples.apache.streams.dlq.DlqObject;
import my.examples.apache.streams.event.DlqEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Slf4j
@Component
@AllArgsConstructor
public class DlqSpringEventPublisher {
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishDlqEvent(final DlqObject dlqObject) {
        //Dlq Produced
        var dlqEvent = new DlqEvent(this, dlqObject);
        log.info(format("A new DLQ event has been generated: %s", dlqEvent));

        applicationEventPublisher.publishEvent(dlqEvent);
    }
}
