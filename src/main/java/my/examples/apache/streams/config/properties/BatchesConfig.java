package my.examples.apache.streams.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "batch")
public class BatchesConfig {
    private Map<String, Integer> items;
}
