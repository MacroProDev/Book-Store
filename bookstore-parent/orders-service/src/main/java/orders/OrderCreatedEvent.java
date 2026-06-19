package orders;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor  // ← Lombok genera el constructor correcto
public class OrderCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private EventHeader header;
    private OrderCreatedEventBody body;
}