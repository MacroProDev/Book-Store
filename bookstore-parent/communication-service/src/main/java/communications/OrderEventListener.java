package communications;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final EmailService emailService;

    @RabbitListener(queues = "${rabbitmq.queue.mails.order-created}")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            emailService.sendOrderCreatedNotification(event);
        } catch (Exception e) {
            log.error("Error al procesar evento de pedido creado: EventId: {}",
                    event.getHeader().getEventId(), e);
            throw e;
        }
    }
}