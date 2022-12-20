package my.examples.apache.streams;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@AllArgsConstructor
@SpringBootApplication
public class StreamsApplication {
	public static void main(String[] args) {
		SpringApplication.run(StreamsApplication.class, args);
	}
}
