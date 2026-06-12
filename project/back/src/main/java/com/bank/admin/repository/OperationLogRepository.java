package com.bank.admin.repository;

import com.bank.admin.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    @Query("SELECT o FROM OperationLog o WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR o.operatorName LIKE CONCAT('%', :keyword, '%') " +
           "OR o.action LIKE CONCAT('%', :keyword, '%') OR o.detail LIKE CONCAT('%', :keyword, '%')) " +
           "AND (:module IS NULL OR :module = '' OR o.module = :module) " +
           "AND (:operatorId IS NULL OR o.operatorId = :operatorId) " +
           "AND (:startTime IS NULL OR o.createdAt >= :startTime) " +
           "AND (:endTime IS NULL OR o.createdAt <= :endTime)")
    Page<OperationLog> search(
            @Param("keyword") String keyword,
            @Param("module") String module,
            @Param("operatorId") Long operatorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
}
