package communications;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "catalogue-service")
public interface CatalogueClient {

    @GetMapping("/api/books")
    List<BookDTO> getAllBooks();
}