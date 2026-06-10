package com.bank.transaction.repository;

import com.bank.transaction.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 账户 Repository（跨模块引用，使用悲观锁保障并发安全）
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * 加悲观写锁查询账户（防止并发转账余额不一致）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithLock(@Param("id") Long id);

    Optional<Account> findByAccountNo(String accountNo);
}
