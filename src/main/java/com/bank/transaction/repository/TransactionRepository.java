package com.bank.transaction.repository;

import com.bank.transaction.entity.Transaction;
import com.bank.transaction.enums.TransactionStatus;
import com.bank.transaction.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByTransactionNo(String transactionNo);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.fromAccountId = :accountId
              AND t.transactionType IN (:types)
              AND t.status = :status
              AND t.createdAt >= :startTime
              AND t.createdAt < :endTime
            """)
    BigDecimal sumAmountByAccountAndTypeAndStatusBetween(
            @Param("accountId") Long accountId,
            @Param("types") List<TransactionType> types,
            @Param("status") TransactionStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.toAccountId = :accountId AND t.status = 'SUCCESS' " +
           "AND t.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal sumIncome(@Param("accountId") Long accountId,
                         @Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.fromAccountId = :accountId AND t.status = 'SUCCESS' " +
           "AND t.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal sumExpense(@Param("accountId") Long accountId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);

    @Query("SELECT t.transactionType, COUNT(t), COALESCE(SUM(t.amount), 0) " +
           "FROM Transaction t " +
           "WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.status = 'SUCCESS' " +
           "AND t.createdAt BETWEEN :startTime AND :endTime " +
           "GROUP BY t.transactionType")
    List<Object[]> groupByTypeStats(@Param("accountId") Long accountId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    @Query("SELECT DATE(t.createdAt), COUNT(t), COALESCE(SUM(t.amount), 0) " +
           "FROM Transaction t " +
           "WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.status = 'SUCCESS' " +
           "AND t.createdAt BETWEEN :startTime AND :endTime " +
           "GROUP BY DATE(t.createdAt) ORDER BY DATE(t.createdAt)")
    List<Object[]> groupByDayStats(@Param("accountId") Long accountId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findForExport(@Param("accountId") Long accountId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    org.springframework.data.domain.Pageable pageable);
}
