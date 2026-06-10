package com.bank.account;

import com.bank.account.config.AccountNumberGenerator;
import com.bank.account.dto.*;
import com.bank.account.entity.Account;
import com.bank.account.enums.AccountStatus;
import com.bank.account.enums.AccountType;
import com.bank.account.exception.*;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService 单元测试")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account activeAccount;
    private static final Long USER_ID = 1L;
    private static final String ACCOUNT_NUMBER = "ACC202401010000001";

    @BeforeEach
    void setUp() {
        activeAccount = Account.builder()
                .id(1L)
                .accountNumber(ACCOUNT_NUMBER)
                .userId(USER_ID)
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("10000.00"))
                .currency("CNY")
                .dailyTransferLimit(new BigDecimal("50000.00"))
                .version(0L)
                .build();
    }

    // ─── 创建账户 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("创建账户 - 成功")
    void createAccount_success() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountType(AccountType.SAVINGS);
        request.setInitialDeposit(new BigDecimal("1000.00"));

        when(accountNumberGenerator.generate()).thenReturn(ACCOUNT_NUMBER);
        when(accountRepository.existsByAccountNumber(ACCOUNT_NUMBER)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(activeAccount);

        AccountResponse response = accountService.createAccount(USER_ID, request);

        assertThat(response).isNotNull();
        assertThat(response.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    // ─── 查询账户 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("按账户ID查询 - 成功")
    void getAccountById_success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));

        AccountResponse response = accountService.getAccountById(USER_ID, 1L);

        assertThat(response.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
        assertThat(response.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("按账户ID查询 - 账户不存在")
    void getAccountById_notFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById(USER_ID, 99L))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("按账户ID查询 - 无权限访问他人账户")
    void getAccountById_accessDenied() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));

        assertThatThrownBy(() -> accountService.getAccountById(999L, 1L))
                .isInstanceOf(AccountAccessDeniedException.class);
    }

    @Test
    @DisplayName("查询用户账户列表")
    void getAccountsByUserId_success() {
        when(accountRepository.findByUserId(USER_ID)).thenReturn(List.of(activeAccount));

        List<AccountResponse> result = accountService.getAccountsByUserId(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(USER_ID);
    }

    // ─── 余额查询 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("查询余额 - 成功")
    void getBalance_success() {
        when(accountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(activeAccount));

        BalanceResponse balance = accountService.getBalance(USER_ID, ACCOUNT_NUMBER);

        assertThat(balance.getBalance()).isEqualByComparingTo(new BigDecimal("10000.00"));
        assertThat(balance.getCurrency()).isEqualTo("CNY");
    }

    // ─── 账户状态 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("冻结账户 - 成功")
    void changeStatus_freeze_success() {
        Account frozenAccount = Account.builder()
                .id(1L).accountNumber(ACCOUNT_NUMBER).userId(USER_ID)
                .accountType(AccountType.SAVINGS).status(AccountStatus.FROZEN)
                .balance(new BigDecimal("10000.00")).currency("CNY").version(1L).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(frozenAccount);

        AccountStatusRequest request = new AccountStatusRequest();
        request.setStatus(AccountStatus.FROZEN);
        request.setReason("用户申请冻结");

        AccountResponse response = accountService.changeAccountStatus(USER_ID, 1L, request);

        assertThat(response.getStatus()).isEqualTo(AccountStatus.FROZEN);
    }

    @Test
    @DisplayName("普通用户不可直接注销账户")
    void changeStatus_user_cannot_close() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));

        AccountStatusRequest request = new AccountStatusRequest();
        request.setStatus(AccountStatus.CLOSED);

        assertThatThrownBy(() -> accountService.changeAccountStatus(USER_ID, 1L, request))
                .isInstanceOf(AccountStatusException.class)
                .hasMessageContaining("注销");
    }

    @Test
    @DisplayName("注销账户余额不为零时报错")
    void adminChangeStatus_close_withBalance_throws() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));

        AccountStatusRequest request = new AccountStatusRequest();
        request.setStatus(AccountStatus.CLOSED);

        assertThatThrownBy(() -> accountService.adminChangeAccountStatus(1L, request))
                .isInstanceOf(AccountStatusException.class)
                .hasMessageContaining("余额");
    }

    @Test
    @DisplayName("注销余额为零的账户 - 成功")
    void adminChangeStatus_close_zeroBalance_success() {
        Account zeroBalanceAccount = Account.builder()
                .id(1L).accountNumber(ACCOUNT_NUMBER).userId(USER_ID)
                .accountType(AccountType.SAVINGS).status(AccountStatus.ACTIVE)
                .balance(BigDecimal.ZERO).currency("CNY").version(0L).build();
        Account closedAccount = Account.builder()
                .id(1L).accountNumber(ACCOUNT_NUMBER).userId(USER_ID)
                .accountType(AccountType.SAVINGS).status(AccountStatus.CLOSED)
                .balance(BigDecimal.ZERO).currency("CNY").version(1L).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(zeroBalanceAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(closedAccount);

        AccountStatusRequest request = new AccountStatusRequest();
        request.setStatus(AccountStatus.CLOSED);

        AccountResponse response = accountService.adminChangeAccountStatus(1L, request);
        assertThat(response.getStatus()).isEqualTo(AccountStatus.CLOSED);
    }

    // ─── 内部余额更新 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("内部余额更新 - 存款成功")
    void updateBalanceInternal_deposit_success() {
        when(accountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(activeAccount));
        when(accountRepository.updateBalance(eq(ACCOUNT_NUMBER),
                eq(new BigDecimal("11000.00")), eq(0L))).thenReturn(1);

        InternalBalanceUpdateRequest request = new InternalBalanceUpdateRequest();
        request.setAccountNumber(ACCOUNT_NUMBER);
        request.setAmount(new BigDecimal("1000.00"));
        request.setOperationType("DEPOSIT");

        assertThatNoException().isThrownBy(() -> accountService.updateBalanceInternal(request));
    }

    @Test
    @DisplayName("内部余额更新 - 余额不足")
    void updateBalanceInternal_withdraw_insufficientBalance() {
        when(accountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(activeAccount));

        InternalBalanceUpdateRequest request = new InternalBalanceUpdateRequest();
        request.setAccountNumber(ACCOUNT_NUMBER);
        request.setAmount(new BigDecimal("-99999.00")); // 超出余额
        request.setOperationType("WITHDRAW");

        assertThatThrownBy(() -> accountService.updateBalanceInternal(request))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("冻结账户不可进行内部余额更新")
    void updateBalanceInternal_frozenAccount_throws() {
        Account frozenAccount = Account.builder()
                .id(1L).accountNumber(ACCOUNT_NUMBER).userId(USER_ID)
                .status(AccountStatus.FROZEN).balance(new BigDecimal("10000.00"))
                .currency("CNY").version(0L).accountType(AccountType.SAVINGS).build();

        when(accountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(frozenAccount));

        InternalBalanceUpdateRequest request = new InternalBalanceUpdateRequest();
        request.setAccountNumber(ACCOUNT_NUMBER);
        request.setAmount(new BigDecimal("1000.00"));
        request.setOperationType("DEPOSIT");

        assertThatThrownBy(() -> accountService.updateBalanceInternal(request))
                .isInstanceOf(AccountStatusException.class);
    }
}
