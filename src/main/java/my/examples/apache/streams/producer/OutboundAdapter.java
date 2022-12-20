package my.examples.apache.streams.producer;

import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;

@FunctionalInterface
public interface OutboundAdapter<S, T> {
    /**
     * send a log to a topic.
     *
     * @param key topic key
     * @param value topic value
     * @return Void
     */
    Mono<Void> send(@NotNull S key, @NotNull T value);
}
