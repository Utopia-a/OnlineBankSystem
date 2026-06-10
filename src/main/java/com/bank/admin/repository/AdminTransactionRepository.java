package com.bank.admin.repository;

import com.bank.transaction.entity.Transaction;
import com.bank.transaction.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminTransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatusAndCreatedAtBetween(TransactionStatus status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.status = 'SUCCESS' AND t.createdAt BETWEEN :start AND :end")
    BigDecimal sumSuccessAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT t.transactionType, COUNT(t), COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.status = 'SUCCESS' AND t.createdAt BETWEEN :start AND :end GROUP BY t.transactionType")
    List<Object[]> countAndSumByTypeAndDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
