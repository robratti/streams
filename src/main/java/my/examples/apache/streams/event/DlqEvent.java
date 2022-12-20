package my.examples.apache.streams.event;

import lombok.Getter;
import my.examples.apache.streams.dlq.DlqObject;
import org.springframework.context.ApplicationEvent;

@Getter
public class DlqEvent extends ApplicationEvent {
    private final DlqObject dlqObject;

    public DlqEvent(Object source, DlqObject dlqObject) {
        super(source);
        this.dlqObject = dlqObject;
    }
}