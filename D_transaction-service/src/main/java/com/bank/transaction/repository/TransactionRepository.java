package com.bank.transaction.repository;

import com.bank.transaction.entity.Transaction;
import com.bank.transaction.enums.TransactionStatus;
import com.bank.transaction.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 交易记录 Repository
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 根据业务流水号查询
     */
    Optional<Transaction> findByTransactionNo(String transactionNo);

    /**
     * 统计指定账户在某日期范围内某类型的交易总额（用于限额校验）
     */
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
            @Param("types") java.util.List<TransactionType> types,
            @Param("status") TransactionStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
