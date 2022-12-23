package my.examples.apache.streams.producer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.examples.apache.streams.dlq.DlqObject;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;

@Slf4j
@Component
@AllArgsConstructor
public class DLQProducer implements OutboundAdapter<String, DlqObject> {
    private final KafkaTemplate<String, DlqObject> dlqKafkaTemplate;

    @Override
    public Mono<Void> send(@NotNull String key, @NotNull DlqObject value) {
        return Mono.fromFuture(() -> {
                    try {
                        return dlqKafkaTemplate.sendDefault(key, value);
                    } catch (Exception e) {
                        log.error(format(
                                "ALERT12001: An Error has been thrown when sending the following object %s to DLQ %s",
                                value,
                                e
                        ));
                        return CompletableFuture.failedFuture(e);
                    }
                })
                .doOnSuccess(stringShippingNotificationSendResult -> log.debug(format(
                                "Log Sent to DLQ topic (Offset: %s, Partition: %s)",
                                stringShippingNotificationSendResult.getRecordMetadata().offset(),
                                stringShippingNotificationSendResult.getRecordMetadata().partition()
                        )
                ))
                .onErrorReturn(new SendResult<>(new ProducerRecord<>("dlq-error", null), null))
                .then();
    }
}