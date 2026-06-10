package com.bank.admin.repository;

import com.banking.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AdminUserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR u.username LIKE CONCAT('%', :keyword, '%') " +
           "OR u.email LIKE CONCAT('%', :keyword, '%') OR u.fullName LIKE CONCAT('%', :keyword, '%')) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:role IS NULL OR u.role = :role)")
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("status") User.UserStatus status,
            @Param("role") User.Role role,
            Pageable pageable);

    long countByStatus(User.UserStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
