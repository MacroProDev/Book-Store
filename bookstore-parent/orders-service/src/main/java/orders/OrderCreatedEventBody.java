package orders;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEventBody {
    private Long orderIDUser;
    private String userEmail;
    private String userName;
    private LocalDateTime orderDate;
    private Double total;
    private String status;
    private List<OrderItemEvent> orderItems;
}