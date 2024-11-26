package my.examples.apache.streams;

import lombok.AllArgsConstructor;
import my.examples.apache.streams.config.properties.BatchesConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AllArgsConstructor
@SpringBootApplication
@EnableConfigurationProperties(BatchesConfig.class)
public class StreamsApplication {
	public static void main(String[] args) {
		SpringApplication.run(StreamsApplication.class, args);
	}
}
