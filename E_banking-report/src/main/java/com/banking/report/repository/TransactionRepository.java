package com.banking.report.repository;

import com.banking.report.entity.TransactionRecord;
import com.banking.report.entity.TransactionRecord.TransactionStatus;
import com.banking.report.entity.TransactionRecord.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface TransactionRepository
        extends JpaRepository<TransactionRecord, Long>,
                JpaSpecificationExecutor<TransactionRecord> {

    Optional<TransactionRecord> findByTransactionNo(String transactionNo);

    // ===== 按账户查询（分页） =====

    @Query("SELECT t FROM TransactionRecord t WHERE " +
           "(t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "ORDER BY t.createdAt DESC")
    Page<TransactionRecord> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    // ===== 按账户 + 时间范围（分页） =====

    @Query("SELECT t FROM TransactionRecord t WHERE " +
           "(t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY t.createdAt DESC")
    Page<TransactionRecord> findByAccountIdAndDateRange(
            @Param("accountId") Long accountId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    // ===== 按账户 + 时间范围（不分页，用于导出） =====

    @Query("SELECT t FROM TransactionRecord t WHERE " +
           "(t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY t.createdAt DESC")
    List<TransactionRecord> findByAccountIdAndDateRangeAll(
            @Param("accountId") Long accountId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // ===== 按用户ID查询所有账户的交易（分页） =====

    @Query("SELECT t FROM TransactionRecord t " +
           "JOIN AccountSummary a ON (a.id = t.fromAccountId OR a.id = t.toAccountId) " +
           "WHERE a.userId = :userId " +
           "ORDER BY t.createdAt DESC")
    Page<TransactionRecord> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // ===== 统计：账户收支汇总 =====

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionRecord t " +
           "WHERE t.toAccountId = :accountId AND t.status = 'SUCCESS' " +
           "AND t.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal sumIncome(@Param("accountId") Long accountId,
                         @Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionRecord t " +
           "WHERE t.fromAccountId = :accountId AND t.status = 'SUCCESS' " +
           "AND t.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal sumExpense(@Param("accountId") Long accountId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);

    // ===== 统计：按类型分组计数 =====

    @Query("SELECT t.transactionType, COUNT(t), COALESCE(SUM(t.amount), 0) " +
           "FROM TransactionRecord t " +
           "WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.status = 'SUCCESS' " +
           "AND t.createdAt BETWEEN :startTime AND :endTime " +
           "GROUP BY t.transactionType")
    List<Object[]> groupByTypeStats(@Param("accountId") Long accountId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    // ===== 统计：按天分组（月报表用） =====

    @Query("SELECT DATE(t.createdAt) as txDate, COUNT(t), COALESCE(SUM(t.amount), 0) " +
           "FROM TransactionRecord t " +
           "WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.status = 'SUCCESS' " +
           "AND t.createdAt BETWEEN :startTime AND :endTime " +
           "GROUP BY DATE(t.createdAt) ORDER BY txDate")
    List<Object[]> groupByDayStats(@Param("accountId") Long accountId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    // ===== 导出：按账户获取全量数据（用于大批量导出，带数量限制） =====

    @Query("SELECT t FROM TransactionRecord t WHERE " +
           "(t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY t.createdAt DESC")
    List<TransactionRecord> findForExport(@Param("accountId") Long accountId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           Pageable pageable);
}
