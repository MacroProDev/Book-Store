package orders;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "catalogue-service")
public interface CatalogueClient {
    @GetMapping("/api/books/{id}")
    BookDTO getBookById(@PathVariable("id") Long id);

    @PatchMapping("/api/books/{id}/stock")
    void updateStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);
}