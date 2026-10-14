package com.example;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagedUserRepository extends JpaRepository<ManagedUserEntity, Long> {}
