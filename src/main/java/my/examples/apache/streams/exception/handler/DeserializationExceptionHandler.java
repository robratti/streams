package my.examples.apache.streams.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.util.Map;

import static java.lang.String.format;

@Slf4j
public class DeserializationExceptionHandler implements org.apache.kafka.streams.errors.DeserializationExceptionHandler {
    @Override
    public DeserializationHandlerResponse handle(
            ProcessorContext context,
            ConsumerRecord<byte[], byte[]> consumerRecord,
            Exception exception
    ) {
        log.error(format(
                "ALERT12003: An Exception was thrown partition %s, offset %s: %s",
                context.partition(),
                context.partition(),
                exception)
        );

        return DeserializationHandlerResponse.CONTINUE;
    }

    @Override
    public void configure(Map<String, ?> configs) {
        log.debug(format("Configuring Deserialization Exception Handler %s", configs));
    }
}
