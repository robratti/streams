package my.examples.apache.streams.dtl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sum implements Serializable {
    private String name;
    private Integer quantity;
}
