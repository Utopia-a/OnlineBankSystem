package com.bank.account.service;

import com.bank.account.config.AccountNumberGenerator;
import com.bank.account.dto.*;
import com.bank.account.entity.Account;
import com.bank.account.enums.AccountStatus;
import com.bank.account.exception.*;
import com.bank.account.repository.AccountRepository;
import com.bank.admin.support.AdminAuditHelper;
import com.banking.auth.entity.User;
import com.banking.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账户管理服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AdminAuditHelper adminAuditHelper;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // 创建账户
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {
        log.info("用户 {} 创建账户，类型: {}", userId, request.getAccountType());

        validateUserCanOpenAccount(userId);

        // 生成唯一账户号
        String accountNumber = generateUniqueAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .userId(userId)
                .accountType(request.getAccountType())
                .status(AccountStatus.ACTIVE)
                .balance(request.getInitialDeposit() != null
                        ? request.getInitialDeposit() : BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "CNY")
                .alias(request.getAlias())
                .dailyTransferLimit(request.getDailyTransferLimit() != null
                        ? request.getDailyTransferLimit() : new BigDecimal("50000.00"))
                .remark(request.getRemark())
                .build();

        account = accountRepository.save(account);
        log.info("账户创建成功，账户号: {}", accountNumber);
        return AccountResponse.fromEntity(account);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 查询账户
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public AccountResponse getAccountById(Long userId, Long accountId) {
        Account account = findAccountById(accountId);
        checkOwnership(account, userId);
        return AccountResponse.fromEntity(account);
    }

    @Override
    public AccountResponse getAccountByNumber(Long userId, String accountNumber) {
        Account account = findAccountByNumber(accountNumber);
        checkOwnership(account, userId);
        return AccountResponse.fromEntity(account);
    }

    @Override
    public List<AccountResponse> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AccountResponse> getAccountsByUserIdPaged(Long userId, Pageable pageable) {
        return accountRepository.findByUserId(userId, pageable)
                .map(AccountResponse::fromEntity);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 更新账户
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AccountResponse updateAccount(Long userId, Long accountId, UpdateAccountRequest request) {
        log.info("用户 {} 更新账户 {}", userId, accountId);

        Account account = findAccountById(accountId);
        checkOwnership(account, userId);

        // 不允许对已注销账户做修改
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountStatusException("已注销账户不可修改");
        }

        if (request.getAlias() != null) {
            account.setAlias(request.getAlias());
        }
        if (request.getDailyTransferLimit() != null) {
            account.setDailyTransferLimit(request.getDailyTransferLimit());
        }
        if (request.getRemark() != null) {
            account.setRemark(request.getRemark());
        }

        account = accountRepository.save(account);
        log.info("账户 {} 信息更新成功", accountId);
        return AccountResponse.fromEntity(account);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 账户状态管理
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AccountResponse changeAccountStatus(Long userId, Long accountId,
                                                AccountStatusRequest request) {
        Account account = findAccountById(accountId);
        checkOwnership(account, userId);

        // 普通用户只允许：ACTIVE <-> FROZEN，不允许直接 CLOSED
        if (request.getStatus() == AccountStatus.CLOSED) {
            throw new AccountStatusException("普通用户不可直接注销账户，请联系管理员");
        }

        return doChangeStatus(account, request);
    }

    @Override
    @Transactional
    public AccountResponse adminChangeAccountStatus(Long accountId, AccountStatusRequest request) {
        Account account = findAccountById(accountId);
        AccountStatus oldStatus = account.getStatus();
        AccountResponse response = doChangeStatus(account, request);
        adminAuditHelper.log("账户管理", "变更账户状态",
                "ACCOUNT", String.valueOf(accountId),
                oldStatus + " -> " + request.getStatus());
        return response;
    }

    private AccountResponse doChangeStatus(Account account, AccountStatusRequest request) {
        AccountStatus newStatus = request.getStatus();
        AccountStatus currentStatus = account.getStatus();

        log.info("账户 {} 状态变更: {} -> {}", account.getAccountNumber(), currentStatus, newStatus);

        // 状态转换合法性校验
        validateStatusTransition(currentStatus, newStatus);

        account.setStatus(newStatus);

        if (newStatus == AccountStatus.CLOSED) {
            // 注销时余额必须为 0
            if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                throw new AccountStatusException("账户余额不为零，无法注销。请先取出余额：" + account.getBalance());
            }
            account.setClosedAt(LocalDateTime.now());
        }

        account = accountRepository.save(account);
        return AccountResponse.fromEntity(account);
    }

    private void validateStatusTransition(AccountStatus from, AccountStatus to) {
        if (from == AccountStatus.CLOSED) {
            throw new AccountStatusException("已注销账户不可更改状态");
        }
        if (from == to) {
            throw new AccountStatusException("账户已经是 " + to + " 状态");
        }
        // FROZEN 账户可以 ACTIVE 或 CLOSED，不限制
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 余额查询
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public BalanceResponse getBalance(Long userId, String accountNumber) {
        Account account = findAccountByNumber(accountNumber);
        checkOwnership(account, userId);
        return new BalanceResponse(
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 内部接口（供成员 D 交易服务调用）
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void updateBalanceInternal(InternalBalanceUpdateRequest request) {
        log.info("内部余额更新 - 账户: {}, 操作: {}, 金额: {}",
                request.getAccountNumber(), request.getOperationType(), request.getAmount());

        Account account = findAccountByNumber(request.getAccountNumber());

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountStatusException("账户 " + request.getAccountNumber() + " 状态异常，无法操作");
        }

        BigDecimal newBalance = account.getBalance().add(request.getAmount());

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException(account.getBalance(),
                    request.getAmount().negate());
        }

        // 使用乐观锁更新
        int updated = accountRepository.updateBalance(
                account.getAccountNumber(), newBalance, account.getVersion());

        if (updated == 0) {
            // 乐观锁冲突，重试
            log.warn("账户 {} 余额更新乐观锁冲突，重试", request.getAccountNumber());
            account = findAccountByNumber(request.getAccountNumber());
            newBalance = account.getBalance().add(request.getAmount());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientBalanceException(account.getBalance(),
                        request.getAmount().negate());
            }
            accountRepository.updateBalance(account.getAccountNumber(), newBalance, account.getVersion());
        }

        log.info("账户 {} 余额更新成功，新余额: {}", request.getAccountNumber(), newBalance);
    }

    @Override
    public boolean validateAccountForTransaction(String accountNumber, BigDecimal amount) {
        try {
            Account account = findAccountByNumber(accountNumber);
            if (account.getStatus() != AccountStatus.ACTIVE) {
                return false;
            }
            if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                return account.getBalance().compareTo(amount) >= 0;
            }
            return true;
        } catch (AccountNotFoundException e) {
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 管理员接口（供成员 F 管理后台使用）
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public Page<AccountResponse> adminGetAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(AccountResponse::fromEntity);
    }

    @Override
    public Page<AccountResponse> adminGetAccountsByUserId(Long userId, Pageable pageable) {
        return accountRepository.findByUserId(userId, pageable).map(AccountResponse::fromEntity);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 私有辅助方法
    // ─────────────────────────────────────────────────────────────────────────

    private Account findAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("账户号码", accountNumber));
    }

    private void checkOwnership(Account account, Long userId) {
        if (!account.getUserId().equals(userId)) {
            throw new AccountAccessDeniedException();
        }
    }

    private void validateUserCanOpenAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AccountStatusException("用户不存在，无法开户"));
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new AccountStatusException("当前用户状态异常，无法开户，请联系管理员");
        }
        if (accountRepository.countByUserIdAndStatus(userId, AccountStatus.FROZEN) > 0) {
            throw new AccountStatusException("存在冻结账户，暂不能新开账户，请联系管理员解冻后再试");
        }
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        int maxRetry = 5;
        do {
            accountNumber = accountNumberGenerator.generate();
            maxRetry--;
        } while (accountRepository.existsByAccountNumber(accountNumber) && maxRetry > 0);

        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new RuntimeException("账户号码生成失败，请重试");
        }
        return accountNumber;
    }
}
