package my.examples.apache.streams.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.examples.apache.streams.exception.handler.DeserializationExceptionHandler;
import my.examples.apache.streams.exception.handler.ProductionExceptionHandler;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Properties;

@Slf4j
@Configuration
@AllArgsConstructor
public class KafkaConfig {
    private Environment environment;

    public Properties getKafkaProperties() {
        var properties = new Properties();
        properties.put(
                StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
                environment.getProperty("spring.kafka.properties.bootstrap.servers")
        );
        properties.put(
                StreamsConfig.APPLICATION_ID_CONFIG,
                environment.getProperty("spring.kafka.properties.application-id")
        );
        properties.put(
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                environment.getProperty("spring.kafka.properties.schema.registry.url")
        );
        properties.put(
                StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                DeserializationExceptionHandler.class
        );
        properties.put(
                StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG,
                ProductionExceptionHandler.class
        );

        if (!Arrays.asList(environment.getActiveProfiles()).contains("local")) {
            properties.put(
                    AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE,
                    environment.getProperty("spring.kafka.properties.basic.auth.credentials.source")
            );
            properties.put(
                    AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG,
                    environment.getProperty("spring.kafka.properties.schema.registry.basic.auth.user.info")
            );
            properties.put(
                    CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
                    environment.getProperty("spring.kafka.properties.security.protocol")
            );
            properties.put(
                    SaslConfigs.SASL_MECHANISM,
                    environment.getProperty("spring.kafka.properties.sasl.mechanism")
            );
            properties.put(
                    SaslConfigs.SASL_JAAS_CONFIG,
                    environment.getProperty("spring.kafka.properties.sasl.jaas.config")
            );
        }

        return properties;
    }
}
