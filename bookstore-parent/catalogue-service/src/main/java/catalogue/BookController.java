package catalogue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @GetMapping
    public List<Book> getAllBooks() {
        // Solo libros visibles para el público
        return bookRepository.findAll().stream().filter(Book::getVisible).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book createBook(@RequestBody Book book) {
        return bookRepository.save(book);
    }

    // Endpoint de búsqueda por título
    @GetMapping("/search/title")
    public List<Book> searchByTitle(@RequestParam String q) {
        return bookRepository.findByTitleContainingIgnoreCase(q);
    }
    
 // PUT - modificación total
    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book bookDetails) {
        Book existingBook = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        
        // Actualizar todos los campos
        existingBook.setTitle(bookDetails.getTitle());
        existingBook.setAuthor(bookDetails.getAuthor());
        existingBook.setPublicationDate(bookDetails.getPublicationDate());
        existingBook.setCategory(bookDetails.getCategory());
        existingBook.setIsbn(bookDetails.getIsbn());
        existingBook.setRating(bookDetails.getRating());
        existingBook.setVisible(bookDetails.getVisible());
        existingBook.setStock(bookDetails.getStock());
        
        return bookRepository.save(existingBook);
    }

    // PATCH - modificación parcial
    @PatchMapping("/{id}")
    public Book patchBook(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Book existingBook = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        
        // Aplicar solo los campos presentes en el mapa
        updates.forEach((key, value) -> {
            switch (key) {
                case "title": existingBook.setTitle((String) value); break;
                case "author": existingBook.setAuthor((String) value); break;
                case "publicationDate": existingBook.setPublicationDate(LocalDate.parse((String) value)); break;
                case "category": existingBook.setCategory((String) value); break;
                case "isbn": existingBook.setIsbn((String) value); break;
                case "rating": existingBook.setRating((Integer) value); break;
                case "visible": existingBook.setVisible((Boolean) value); break;
                case "stock": existingBook.setStock((Integer) value); break;
            }
        });
        
        return bookRepository.save(existingBook);
    }

    // DELETE - eliminar un libro
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        bookRepository.delete(book);
        return ResponseEntity.ok().build();  // 200 OK
    }
    
    @GetMapping("/search")
    public List<Book> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Boolean visible,
            @RequestParam(required = false) String publicationDate) {
        
        LocalDate date = null;
        if (publicationDate != null) {
            date = LocalDate.parse(publicationDate);
        }
        
        return bookRepository.searchBooks(title, author, category, isbn, rating, visible, date);
    }
    
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Book> updateStock(@PathVariable Long id, @RequestParam Integer quantity) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found"));
        if (book.getStock() < quantity) {
            throw new RuntimeException("Stock insuficiente");
        }
        book.setStock(book.getStock() - quantity);
        return ResponseEntity.ok(bookRepository.save(book));
    }
}