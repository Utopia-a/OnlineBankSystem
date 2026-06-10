package com.bank.transaction;

import com.bank.transaction.config.TransactionNoGenerator;
import com.bank.transaction.config.TransactionProperties;
import com.bank.transaction.dto.DepositRequest;
import com.bank.transaction.dto.TransactionResponse;
import com.bank.transaction.dto.TransferRequest;
import com.bank.transaction.dto.WithdrawRequest;
import com.bank.account.entity.Account;
import com.bank.account.enums.AccountStatus;
import com.bank.account.repository.AccountRepository;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.enums.TransactionStatus;
import com.bank.transaction.exception.AccountStatusException;
import com.bank.transaction.exception.InsufficientBalanceException;
import com.bank.transaction.exception.TransactionLimitException;
import com.bank.transaction.repository.TransactionRepository;
import com.bank.transaction.service.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 交易服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService 单元测试")
class TransactionServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private TransactionNoGenerator transactionNoGenerator;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private TransactionProperties properties;

    @BeforeEach
    void setUp() {
        properties = new TransactionProperties();
        // 通过反射注入 properties
        try {
            var field = TransactionServiceImpl.class.getDeclaredField("properties");
            field.setAccessible(true);
            field.set(transactionService, properties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(transactionNoGenerator.generate()).thenReturn("TXN202406101234560001");
        when(transactionRepository.save(any())).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(1L);
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });
    }

    // ─── 存款测试 ───────────────────────────────────────────

    @Test
    @DisplayName("存款 - 正常存款成功")
    void deposit_success() {
        Account account = buildAccount(1001L, new BigDecimal("5000.00"), AccountStatus.ACTIVE);
        when(accountRepository.findByIdWithLock(1001L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenReturn(account);

        DepositRequest req = new DepositRequest();
        req.setAccountId(1001L);
        req.setAmount(new BigDecimal("1000.00"));
        req.setRemark("测试存款");

        TransactionResponse resp = transactionService.deposit(req, 1L, "127.0.0.1");

        assertThat(resp.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(resp.getToBalanceAfter()).isEqualByComparingTo("6000.00");
    }

    @Test
    @DisplayName("存款 - 账户冻结时拒绝")
    void deposit_frozenAccount_throws() {
        Account account = buildAccount(1001L, new BigDecimal("5000.00"), AccountStatus.FROZEN);
        when(accountRepository.findByIdWithLock(1001L)).thenReturn(Optional.of(account));

        DepositRequest req = new DepositRequest();
        req.setAccountId(1001L);
        req.setAmount(new BigDecimal("100.00"));

        assertThatThrownBy(() -> transactionService.deposit(req, 1L, "127.0.0.1"))
                .isInstanceOf(AccountStatusException.class);
    }

    // ─── 取款测试 ───────────────────────────────────────────

    @Test
    @DisplayName("取款 - 正常取款成功")
    void withdraw_success() {
        Account account = buildAccount(1001L, new BigDecimal("5000.00"), AccountStatus.ACTIVE);
        when(accountRepository.findByIdWithLock(1001L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenReturn(account);
        when(transactionRepository.sumAmountByAccountAndTypeAndStatusBetween(
                anyLong(), anyList(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        WithdrawRequest req = new WithdrawRequest();
        req.setAccountId(1001L);
        req.setAmount(new BigDecimal("2000.00"));

        TransactionResponse resp = transactionService.withdraw(req, 1L, "127.0.0.1");

        assertThat(resp.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(resp.getFromBalanceAfter()).isEqualByComparingTo("3000.00");
    }

    @Test
    @DisplayName("取款 - 余额不足时拒绝")
    void withdraw_insufficientBalance_throws() {
        Account account = buildAccount(1001L, new BigDecimal("100.00"), AccountStatus.ACTIVE);
        when(accountRepository.findByIdWithLock(1001L)).thenReturn(Optional.of(account));
        when(transactionRepository.sumAmountByAccountAndTypeAndStatusBetween(
                anyLong(), anyList(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        WithdrawRequest req = new WithdrawRequest();
        req.setAccountId(1001L);
        req.setAmount(new BigDecimal("500.00"));

        assertThatThrownBy(() -> transactionService.withdraw(req, 1L, "127.0.0.1"))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("取款 - 超出单日限额时拒绝")
    void withdraw_exceedDailyLimit_throws() {
        Account account = buildAccount(1001L, new BigDecimal("100000.00"), AccountStatus.ACTIVE);
        when(accountRepository.findByIdWithLock(1001L)).thenReturn(Optional.of(account));
        // 今日已取款 49900，再取 200 超过 50000 限额
        when(transactionRepository.sumAmountByAccountAndTypeAndStatusBetween(
                anyLong(), anyList(), any(), any(), any()))
                .thenReturn(new BigDecimal("49900.00"));

        WithdrawRequest req = new WithdrawRequest();
        req.setAccountId(1001L);
        req.setAmount(new BigDecimal("200.00"));

        assertThatThrownBy(() -> transactionService.withdraw(req, 1L, "127.0.0.1"))
                .isInstanceOf(TransactionLimitException.class)
                .hasMessageContaining("单日取款限额");
    }

    // ─── 转账测试 ───────────────────────────────────────────

    @Test
    @DisplayName("转账 - 正常转账成功")
    void transfer_success() {
        Account fromAccount = buildAccount(1001L, new BigDecimal("5000.00"), AccountStatus.ACTIVE);
        Account toAccount   = buildAccount(1002L, new BigDecimal("1000.00"), AccountStatus.ACTIVE);

        when(accountRepository.findByIdWithLock(1001L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIdWithLock(1002L)).thenReturn(Optional.of(toAccount));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.sumAmountByAccountAndTypeAndStatusBetween(
                anyLong(), anyList(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        TransferRequest req = new TransferRequest();
        req.setFromAccountId(1001L);
        req.setToAccountId(1002L);
        req.setAmount(new BigDecimal("1000.00"));
        req.setRemark("测试转账");

        TransactionResponse resp = transactionService.transfer(req, 1L, "127.0.0.1");

        assertThat(resp.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(resp.getFromBalanceAfter()).isEqualByComparingTo("4000.00");
        assertThat(resp.getToBalanceAfter()).isEqualByComparingTo("2000.00");
    }

    @Test
    @DisplayName("转账 - 自转账时拒绝")
    void transfer_sameAccount_throws() {
        TransferRequest req = new TransferRequest();
        req.setFromAccountId(1001L);
        req.setToAccountId(1001L);
        req.setAmount(new BigDecimal("100.00"));

        assertThatThrownBy(() -> transactionService.transfer(req, 1L, "127.0.0.1"))
                .isInstanceOf(com.bank.transaction.exception.BusinessException.class)
                .hasMessageContaining("不能向自己账户转账");
    }

    // ─── 工具方法 ────────────────────────────────────────────

    private Account buildAccount(Long id, BigDecimal balance, AccountStatus status) {
        Account a = new Account();
        a.setId(id);
        a.setAccountNumber("ACC" + id);
        a.setBalance(balance);
        a.setStatus(status);
        a.setVersion(0L);
        return a;
    }
}
