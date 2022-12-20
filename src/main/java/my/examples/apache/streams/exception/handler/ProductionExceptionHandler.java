package my.examples.apache.streams.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;

import static java.lang.String.format;

@Slf4j
public class ProductionExceptionHandler implements org.apache.kafka.streams.errors.ProductionExceptionHandler {
    @Override
    public ProductionExceptionHandlerResponse handle(ProducerRecord<byte[], byte[]> recordAlert, Exception exception) {
        log.error(format("ALERT12002: Exception was thrown: %s", exception));
        return ProductionExceptionHandlerResponse.FAIL;
    }

    @Override
    public void configure(Map<String, ?> configs) {
        log.debug(format("production exception configured: %s", configs));
    }
}
