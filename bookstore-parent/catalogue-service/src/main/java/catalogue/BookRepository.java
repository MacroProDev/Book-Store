package catalogue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long>, 
										JpaSpecificationExecutor<Book>, 
										PagingAndSortingRepository<Book, Long> {
    // Búsqueda por título (LIKE)
    List<Book> findByTitleContainingIgnoreCase(String title);
    // Búsqueda por autor
    List<Book> findByAuthorContainingIgnoreCase(String author);

    @Query("SELECT b FROM Book b WHERE " +
       "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
       "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
       "(:category IS NULL OR LOWER(b.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
       "(:isbn IS NULL OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :isbn, '%'))) AND " +
       "(:rating IS NULL OR b.rating = :rating) AND " +
       "(:visible IS NULL OR b.visible = :visible) AND " +
       "(:publicationDate IS NULL OR b.publicationDate = :publicationDate) ")  
    
    List<Book> searchBooks(@Param("title") String title,
                       @Param("author") String author,
                       @Param("category") String category,
                       @Param("isbn") String isbn,
                       @Param("rating") Integer rating,
                       @Param("visible") Boolean visible,
                       @Param("publicationDate") LocalDate publicationDate);
}