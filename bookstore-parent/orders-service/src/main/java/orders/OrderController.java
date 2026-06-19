package orders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CatalogueClient catalogueClient;

    @Autowired
    private final OrderEventService orderEventService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestBody OrderRequest request,
            @RequestHeader("accessToken") String accessToken) {

        // Verificar que el userId del token coincide con el del request
        Long tokenUserId = jwtUtils.getUserIdFromToken(accessToken);
        if (tokenUserId == null || !tokenUserId.equals(request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "No autorizado para crear pedidos para otro usuario");
        }

        if (request.getUserId() == null || request.getOrders() == null || request.getOrders().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos inválidos");
        }

        double total = 0.0;
        for (OrderItem item : request.getOrders()) {
            BookDTO book;
            try {
                book = catalogueClient.getBookById(item.getBookId());
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Libro no encontrado: " + item.getBookId());
            }
            if (book.getVisible() == null || !book.getVisible()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El libro no está disponible: " + item.getBookId());
            }
            if (book.getStock() == null || book.getStock() < item.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stock insuficiente para el libro: " + item.getBookId());
            }
            item.setPrice(book.getPrice() * item.getQuantity());
            total += item.getPrice();
        }

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setUserName(request.getUserName());
        order.setOrderItems(request.getOrders());
        order.setTotalPrice(total);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("COMPLETED");

        Order saved = orderRepository.save(order);
        orderEventService.publishOrderCreatedEvent(saved, request.getUserEmail());

        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(
            @RequestHeader("accessToken") String accessToken) {

        Long tokenUserId = jwtUtils.getUserIdFromToken(accessToken);
        if (tokenUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }

        // El usuario solo puede ver sus propios pedidos
        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(tokenUserId);
        return ResponseEntity.ok(orders);
    }
}