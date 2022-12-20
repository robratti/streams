package my.examples.apache.streams.exception.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.examples.apache.streams.dlq.DlqObject;
import my.examples.apache.streams.publisher.DlqSpringEventPublisher;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;

import static java.lang.String.format;

@Slf4j
@AllArgsConstructor
public class UncaughtExceptionHandler implements StreamsUncaughtExceptionHandler {
    private final DlqSpringEventPublisher dlqSpringEventPublisher;

    @Override
    public StreamThreadExceptionResponse handle(Throwable throwable) {
        log.error(format("ALERT12001: An uncaught exception was thrown: %s", throwable));
        dlqSpringEventPublisher.publishDlqEvent(
                DlqObject.builder()
                        .error(throwable)
                        .build()
        );

        return StreamThreadExceptionResponse.REPLACE_THREAD;
    }
}
