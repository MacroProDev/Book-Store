package communications;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogueContextService {

    private final CatalogueClient catalogueClient;
    private String cataloguePrompt;

    @PostConstruct
    public void loadCatalogue() {
        try {
            List<BookDTO> books = catalogueClient.getAllBooks();
            cataloguePrompt = buildCataloguePrompt(books);
            log.info("Catálogo cargado correctamente: {} libros", books.size());
        } catch (Exception e) {
            log.error("Error al cargar el catálogo: {}", e.getMessage());
            cataloguePrompt = "No se pudo cargar el catálogo de libros en este momento.";
        }
    }

    private String buildCataloguePrompt(List<BookDTO> books) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres un agente de atención al cliente de 'Relatos de Papel', una librería online. ")
          .append("Ayuda a los usuarios con consultas sobre libros, pedidos y envíos. ")
          .append("Responde siempre en español, de forma amable y concisa.\n\n")
          .append("Catálogo disponible:\n");

        for (BookDTO book : books) {
            sb.append(book.getTitle())
              .append(" - ").append(book.getAuthor())
              .append(" (").append(book.getCategory()).append(")")
              .append(" | ").append(book.getPrice()).append("€")
              .append(" | Stock: ").append(book.getStock()).append("\n");
        }

        sb.append("\nResponde preguntas sobre disponibilidad, precios y recomendaciones. ")
          .append("Si un libro no está en el catálogo, indícalo amablemente.");

        return sb.toString();
    }

    public String getCataloguePrompt() {
        return cataloguePrompt;
    }
}