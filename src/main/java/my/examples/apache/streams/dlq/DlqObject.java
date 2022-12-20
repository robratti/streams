package my.examples.apache.streams.dlq;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Optional;

@Data
@Slf4j
@NoArgsConstructor
public class DlqObject<K, V> implements Serializable {
    private Throwable error;
    private K key;
    private V value;

    @Builder
    public DlqObject(Throwable error, @Nullable K key, V value) {
        this.error = error;
        this.key = Optional.ofNullable(key)
                .orElse(null);
        this.value = value;
    }
}
