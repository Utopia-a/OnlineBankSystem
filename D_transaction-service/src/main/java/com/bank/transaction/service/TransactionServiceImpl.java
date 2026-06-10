package com.bank.transaction.service;

import com.bank.transaction.config.TransactionNoGenerator;
import com.bank.transaction.config.TransactionProperties;
import com.bank.transaction.dto.DepositRequest;
import com.bank.transaction.dto.TransactionResponse;
import com.bank.transaction.dto.TransferRequest;
import com.bank.transaction.dto.WithdrawRequest;
import com.bank.transaction.entity.Account;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.enums.TransactionStatus;
import com.bank.transaction.enums.TransactionType;
import com.bank.transaction.exception.*;
import com.bank.transaction.repository.AccountRepository;
import com.bank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易服务实现
 *
 * <p><b>事务策略：</b>
 * <ul>
 *   <li>所有写操作使用 {@code REPEATABLE_READ} 隔离级别</li>
 *   <li>转账对两个账户按 ID 升序排序后依次加悲观写锁，避免死锁</li>
 *   <li>任意步骤失败均 rollback，流水号记录失败原因</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionNoGenerator transactionNoGenerator;
    private final TransactionProperties properties;

    // ─────────────────────────────────────────────────────
    //  转账
    // ─────────────────────────────────────────────────────

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public TransactionResponse transfer(TransferRequest request,
                                        Long operatorId,
                                        String operatorIp) {

        BigDecimal amount = request.getAmount();
        Long fromId = request.getFromAccountId();
        Long toId = request.getToAccountId();

        log.info("转账开始 | from={} to={} amount={} operator={}",
                fromId, toId, amount, operatorId);

        // 1. 参数校验
        if (fromId.equals(toId)) {
            throw new BusinessException(4005, "不能向自己账户转账");
        }
        validateTransferAmount(amount);

        // 2. 按 ID 排序加锁（死锁预防）
        Account fromAccount;
        Account toAccount;
        if (fromId < toId) {
            fromAccount = lockAccount(fromId);
            toAccount   = lockAccount(toId);
        } else {
            toAccount   = lockAccount(toId);
            fromAccount = lockAccount(fromId);
        }

        // 3. 账户状态校验
        validateAccountActive(fromAccount);
        validateAccountActive(toAccount);

        // 4. 限额校验
        validateTransferDailyLimit(fromId, amount);

        // 5. 余额校验
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

        // 6. 执行余额变更
        BigDecimal fromBefore = fromAccount.getBalance();
        BigDecimal toBefore   = toAccount.getBalance();

        fromAccount.setBalance(fromBefore.subtract(amount));
        toAccount.setBalance(toBefore.add(amount));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 7. 记录流水
        Transaction txn = Transaction.builder()
                .transactionNo(transactionNoGenerator.generate())
                .transactionType(TransactionType.TRANSFER_OUT)
                .status(TransactionStatus.SUCCESS)
                .fromAccountId(fromId)
                .toAccountId(toId)
                .amount(amount)
                .fromBalanceBefore(fromBefore)
                .fromBalanceAfter(fromAccount.getBalance())
                .toBalanceBefore(toBefore)
                .toBalanceAfter(toAccount.getBalance())
                .remark(request.getRemark())
                .operatorId(operatorId)
                .operatorIp(operatorIp)
                .build();
        transactionRepository.save(txn);

        log.info("转账成功 | txnNo={} from={} to={} amount={}",
                txn.getTransactionNo(), fromId, toId, amount);

        return toResponse(txn);
    }

    // ─────────────────────────────────────────────────────
    //  存款
    // ─────────────────────────────────────────────────────

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public TransactionResponse deposit(DepositRequest request,
                                       Long operatorId,
                                       String operatorIp) {

        BigDecimal amount    = request.getAmount();
        Long       accountId = request.getAccountId();

        log.info("存款开始 | accountId={} amount={} operator={}", accountId, amount, operatorId);

        // 1. 金额校验
        validateDepositAmount(amount);

        // 2. 账户加锁
        Account account = lockAccount(accountId);
        validateAccountActive(account);

        // 3. 余额变更
        BigDecimal before = account.getBalance();
        account.setBalance(before.add(amount));
        accountRepository.save(account);

        // 4. 记录流水
        Transaction txn = Transaction.builder()
                .transactionNo(transactionNoGenerator.generate())
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .toAccountId(accountId)
                .amount(amount)
                .toBalanceBefore(before)
                .toBalanceAfter(account.getBalance())
                .remark(request.getRemark())
                .operatorId(operatorId)
                .operatorIp(operatorIp)
                .build();
        transactionRepository.save(txn);

        log.info("存款成功 | txnNo={} accountId={} amount={}",
                txn.getTransactionNo(), accountId, amount);

        return toResponse(txn);
    }

    // ─────────────────────────────────────────────────────
    //  取款
    // ─────────────────────────────────────────────────────

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public TransactionResponse withdraw(WithdrawRequest request,
                                        Long operatorId,
                                        String operatorIp) {

        BigDecimal amount    = request.getAmount();
        Long       accountId = request.getAccountId();

        log.info("取款开始 | accountId={} amount={} operator={}", accountId, amount, operatorId);

        // 1. 金额校验
        validateWithdrawAmount(amount);

        // 2. 账户加锁
        Account account = lockAccount(accountId);
        validateAccountActive(account);

        // 3. 限额校验
        validateWithdrawDailyLimit(accountId, amount);

        // 4. 余额校验
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

        // 5. 余额变更
        BigDecimal before = account.getBalance();
        account.setBalance(before.subtract(amount));
        accountRepository.save(account);

        // 6. 记录流水
        Transaction txn = Transaction.builder()
                .transactionNo(transactionNoGenerator.generate())
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCESS)
                .fromAccountId(accountId)
                .amount(amount)
                .fromBalanceBefore(before)
                .fromBalanceAfter(account.getBalance())
                .remark(request.getRemark())
                .operatorId(operatorId)
                .operatorIp(operatorIp)
                .build();
        transactionRepository.save(txn);

        log.info("取款成功 | txnNo={} accountId={} amount={}",
                txn.getTransactionNo(), accountId, amount);

        return toResponse(txn);
    }

    // ─────────────────────────────────────────────────────
    //  私有工具方法
    // ─────────────────────────────────────────────────────

    private Account lockAccount(Long accountId) {
        return accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private void validateAccountActive(Account account) {
        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new AccountStatusException(account.getStatus());
        }
    }

    private void validateTransferAmount(BigDecimal amount) {
        TransactionProperties.Transfer cfg = properties.getTransfer();
        if (amount.compareTo(cfg.getMinAmount()) < 0) {
            throw new TransactionLimitException("转账金额不能小于 " + cfg.getMinAmount());
        }
        if (amount.compareTo(cfg.getSingleLimit()) > 0) {
            throw new TransactionLimitException("单笔转账不能超过 " + cfg.getSingleLimit());
        }
    }

    private void validateDepositAmount(BigDecimal amount) {
        TransactionProperties.Deposit cfg = properties.getDeposit();
        if (amount.compareTo(cfg.getMinAmount()) < 0) {
            throw new TransactionLimitException("存款金额不能小于 " + cfg.getMinAmount());
        }
        if (amount.compareTo(cfg.getMaxAmount()) > 0) {
            throw new TransactionLimitException("单笔存款不能超过 " + cfg.getMaxAmount());
        }
    }

    private void validateWithdrawAmount(BigDecimal amount) {
        TransactionProperties.Withdraw cfg = properties.getWithdraw();
        if (amount.compareTo(cfg.getMinAmount()) < 0) {
            throw new TransactionLimitException("取款金额不能小于 " + cfg.getMinAmount());
        }
        if (amount.compareTo(cfg.getSingleLimit()) > 0) {
            throw new TransactionLimitException("单笔取款不能超过 " + cfg.getSingleLimit());
        }
    }

    private void validateTransferDailyLimit(Long accountId, BigDecimal amount) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = startOfDay.plusDays(1);

        BigDecimal todayTotal = transactionRepository.sumAmountByAccountAndTypeAndStatusBetween(
                accountId,
                List.of(TransactionType.TRANSFER_OUT),
                TransactionStatus.SUCCESS,
                startOfDay, endOfDay
        );

        BigDecimal newTotal = todayTotal.add(amount);
        BigDecimal dailyLimit = properties.getTransfer().getDailyLimit();

        if (newTotal.compareTo(dailyLimit) > 0) {
            throw new TransactionLimitException(
                    String.format("单日转账限额 %s，今日已转 %s，本次 %s，超出限额",
                            dailyLimit, todayTotal, amount));
        }
    }

    private void validateWithdrawDailyLimit(Long accountId, BigDecimal amount) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = startOfDay.plusDays(1);

        BigDecimal todayTotal = transactionRepository.sumAmountByAccountAndTypeAndStatusBetween(
                accountId,
                List.of(TransactionType.WITHDRAWAL),
                TransactionStatus.SUCCESS,
                startOfDay, endOfDay
        );

        BigDecimal newTotal  = todayTotal.add(amount);
        BigDecimal dailyLimit = properties.getWithdraw().getDailyLimit();

        if (newTotal.compareTo(dailyLimit) > 0) {
            throw new TransactionLimitException(
                    String.format("单日取款限额 %s，今日已取 %s，本次 %s，超出限额",
                            dailyLimit, todayTotal, amount));
        }
    }

    private TransactionResponse toResponse(Transaction txn) {
        return TransactionResponse.builder()
                .id(txn.getId())
                .transactionNo(txn.getTransactionNo())
                .transactionType(txn.getTransactionType())
                .status(txn.getStatus())
                .fromAccountId(txn.getFromAccountId())
                .toAccountId(txn.getToAccountId())
                .amount(txn.getAmount())
                .fromBalanceAfter(txn.getFromBalanceAfter())
                .toBalanceAfter(txn.getToBalanceAfter())
                .remark(txn.getRemark())
                .createdAt(txn.getCreatedAt())
                .failureReason(txn.getFailureReason())
                .build();
    }
}
