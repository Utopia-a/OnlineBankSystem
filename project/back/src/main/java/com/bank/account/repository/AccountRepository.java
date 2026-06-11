package com.bank.account.repository;

import com.bank.account.entity.Account;
import com.bank.account.enums.AccountStatus;
import com.bank.account.enums.AccountType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 账户数据访问层
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithLock(@Param("id") Long id);

    boolean existsByIdAndUserId(Long id, Long userId);

    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * 查询用户的所有账户
     */
    List<Account> findByUserId(Long userId);

    /**
     * 查询用户的活跃账户
     */
    List<Account> findByUserIdAndStatus(Long userId, AccountStatus status);

    /**
     * 分页查询用户账户
     */
    Page<Account> findByUserId(Long userId, Pageable pageable);

    /**
     * 检查账户号是否存在
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * 根据用户ID和账户类型查询
     */
    List<Account> findByUserIdAndAccountType(Long userId, AccountType accountType);

    /**
     * 统计用户账户数量
     */
    long countByUserId(Long userId);

    /**
     * 统计用户特定状态账户数量
     */
    long countByUserIdAndStatus(Long userId, AccountStatus status);

    /**
     * 批量查询账户（供交易服务调用）
     */
    @Query("SELECT a FROM Account a WHERE a.accountNumber IN :accountNumbers")
    List<Account> findByAccountNumbers(@Param("accountNumbers") List<String> accountNumbers);

    /**
     * 根据账户号码和用户ID查询（安全校验用）
     */
    Optional<Account> findByAccountNumberAndUserId(String accountNumber, Long userId);

    /**
     * 更新余额（供内部服务调用，乐观锁保护）
     */
    @Modifying
    @Query("UPDATE Account a SET a.balance = :balance, a.version = a.version + 1 " +
           "WHERE a.accountNumber = :accountNumber AND a.version = :version")
    int updateBalance(@Param("accountNumber") String accountNumber,
                      @Param("balance") BigDecimal balance,
                      @Param("version") Long version);

    /**
     * 查询余额大于指定金额的账户
     */
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.balance >= :minBalance AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsWithMinBalance(@Param("userId") Long userId,
                                                    @Param("minBalance") BigDecimal minBalance);
}
