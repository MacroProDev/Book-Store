package communications;



import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import event.OrderCreatedEvent;
import event.OrderItemEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${email.notification.to}")
    private String defaultEmailTo;

    @Value("${email.notification.from}")
    private String emailFrom;

    public void sendOrderCreatedNotification(OrderCreatedEvent event) {
        try {
        	
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(defaultEmailTo);
            message.setFrom(emailFrom);
            message.setSubject("Nuevo Pedido Creado");
            message.setText(buildEmailContent(event));
            mailSender.send(message);
            log.info("Notificación de correo enviada exitosamente para el pedido del usuario: {}",
                    event.getBody().getOrderIDUser());
        } catch (Exception e) {
            log.error("Error al enviar notificación de correo", e);
        }
    }

    private String buildEmailContent(OrderCreatedEvent event) {
        StringBuilder content = new StringBuilder();
        content.append("Ha realizado el siguiente pedido.\n\n");
        content.append("Detalles del Pedido:\n");
        content.append("==================\n");
        content.append("Nombre del cliente: ").append(event.getBody().getUserName()).append("\n");
        content.append("Fecha: ").append(
                event.getBody().getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        ).append("\n");

        content.append("Artículos adquiridos:\n");
        content.append("==========\n");
        for (OrderItemEvent item : event.getBody().getOrderItems()) {
            content.append("- ID Libro: ").append(item.getIdCatalogue()).append("\n");
            content.append("  Cantidad: ").append(item.getQuantity()).append("\n");
            content.append("  Subtotal: ").append(
                    NumberFormat.getCurrencyInstance(new Locale("es", "ES"))
                            .format(item.getSubTotal())
            ).append("\n\n");
        }

        content.append("Total: ").append(
                NumberFormat.getCurrencyInstance(new Locale("es", "ES"))
                        .format(event.getBody().getTotal())
        ).append("\n\n");

        return content.toString();
    }
}