package com.example;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByEmail(String email);
    List<AccountEntity> findAllByRoleOrderByNameAsc(String role);
    List<AccountEntity> findAllByOrderByNameAsc();
    long countByRole(String role);
    long countByRoleAndEnabledTrue(String role);
}
