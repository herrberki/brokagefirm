package com.myproject.brokagefirmchallenge.repo.repository;


import com.myproject.brokagefirmchallenge.repo.entity.Customer;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CustomerRepository extends BaseRepository<Customer, Long> {

    Optional<Customer> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE Customer c SET c.lastLoginDate = :date WHERE c.id = :id")
    void updateLastLoginDate(@Param("id") Long id,
                            @Param("date") LocalDateTime date);

    @Modifying
    @Query("UPDATE Customer c SET c.isLocked = :locked WHERE c.id = :id")
    void updateAccountLockStatus(@Param("id") Long id,
                                @Param("locked") boolean locked);

    @Modifying
    @Query("UPDATE Customer c SET c.failedLoginAttempts = :attempts WHERE c.id = :id")
    void updateFailedLoginAttempts(@Param("id") Long id,
                                  @Param("attempts") int attempts);


}
