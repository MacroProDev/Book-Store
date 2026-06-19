package orders;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.orders}")
    private String ordersExchange;

    @Value("${rabbitmq.routing.key.order.created}")
    private String orderCreatedRoutingKey;

    public void publishOrderCreatedEvent(Order order, String userEmail) {
        try {
            OrderCreatedEvent event = buildOrderCreatedEvent(order, userEmail);
            rabbitTemplate.convertAndSend(ordersExchange, orderCreatedRoutingKey, event);
            log.info("Evento publicado para el pedido del usuario: {}", order.getUserId());
        } catch (Exception e) {
            log.error("Error al publicar evento de pedido creado", e);
        }
    }

    private OrderCreatedEvent buildOrderCreatedEvent(Order order, String userEmail) {
        EventHeader header = EventHeader.builder()
                .eventId(UUID.randomUUID().toString())
                .version("1.0")
                .eventType("ORDER_CREATED")
                .timestamp(LocalDateTime.now())
                .build();

        List<OrderItemEvent> orderItemEvents = order.getOrderItems().stream()
                .map(this::mapToOrderItemEvent)
                .toList();

        OrderCreatedEventBody body = OrderCreatedEventBody.builder()
                .orderIDUser(order.getUserId())
                .userName(order.getUserName())
                .userEmail(userEmail)          // ← nuevo
                .orderDate(order.getOrderDate())
                .total(order.getTotalPrice())
                .status(order.getStatus())
                .orderItems(orderItemEvents)
                .build();

        return OrderCreatedEvent.builder()
                .header(header)
                .body(body)
                .build();
    }

    private OrderItemEvent mapToOrderItemEvent(OrderItem orderItem) {
        return OrderItemEvent.builder()
                .idCatalogue(orderItem.getBookId())
                .quantity(orderItem.getQuantity())
                .subTotal(orderItem.getPrice())
                .build();
    }
}