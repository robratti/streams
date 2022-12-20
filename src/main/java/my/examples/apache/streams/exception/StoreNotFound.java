package my.examples.apache.streams.exception;

import static java.lang.String.format;

public class StoreNotFound extends RuntimeException {
    public StoreNotFound(String locationId) {
        super(format("Store not found for location %s", locationId));
    }
}
