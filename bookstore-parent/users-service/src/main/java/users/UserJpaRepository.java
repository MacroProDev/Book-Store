package users;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Integer> {

    Optional<User> findByCif(String cif);
    Optional<User> findByEmail(String email); 
}