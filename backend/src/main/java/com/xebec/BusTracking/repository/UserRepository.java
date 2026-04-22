package com.xebec.BusTracking.repository;

import com.xebec.BusTracking.model.User;
import com.xebec.BusTracking.model.UserRole;
import com.xebec.BusTracking.model.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRole role);

    @Query("""
      SELECT u FROM User u
      WHERE (:search IS NULL
             OR LOWER(u.firstName) LIKE LOWER(CONCAT('%',:search,'%'))
             OR LOWER(u.lastName)  LIKE LOWER(CONCAT('%',:search,'%'))
             OR LOWER(u.email)     LIKE LOWER(CONCAT('%',:search,'%')))
        AND (:role   IS NULL OR u.role   = :role)
        AND (:status IS NULL OR u.status = :status)
  """)
    Page<User> findFiltered(@Param("search") String search,
                            @Param("role")   UserRole role,
                            @Param("status") UserStatus status,
                            Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    long countByRole(UserRole role);

    long countByStatus(UserStatus status);
}
