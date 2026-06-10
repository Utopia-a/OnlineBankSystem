package com.bank.admin.service.impl;

import com.bank.admin.dto.request.TransactionQueryRequest;
import com.bank.admin.dto.response.DashboardStatsVO;
import com.bank.admin.dto.response.PageResult;
import com.bank.admin.dto.response.TransactionVO;
import com.bank.admin.enums.AdminTransactionType;
import com.bank.admin.repository.AdminTransactionRepository;
import com.bank.admin.repository.AdminUserRepository;
import com.bank.admin.service.TransactionMonitorService;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.enums.TransactionStatus;
import com.bank.transaction.enums.TransactionType;
import com.bank.transaction.exception.BusinessException;
import com.banking.auth.entity.User;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionMonitorServiceImpl implements TransactionMonitorService {

    private final AdminTransactionRepository adminTransactionRepository;
    private final AdminUserRepository adminUserRepository;

    @Override
    public PageResult<TransactionVO> listTransactions(TransactionQueryRequest request) {
        Page<Transaction> page = adminTransactionRepository.findAll(
                buildSpecification(request),
                request.toSpringPageRequest(Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return PageResult.of(page.map(TransactionVO::fromEntity));
    }

    @Override
    public TransactionVO getTransactionById(Long transactionId) {
        Transaction tx = adminTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException(400, "交易记录不存在，ID: " + transactionId));
        return TransactionVO.fromEntity(tx);
    }

    @Override
    public DashboardStatsVO getDashboardStats() {
        DashboardStatsVO stats = new DashboardStatsVO();

        stats.setTotalUsers(adminUserRepository.count());
        stats.setActiveUsers(adminUserRepository.countByStatus(User.UserStatus.ACTIVE));
        stats.setFrozenUsers(adminUserRepository.countByStatus(User.UserStatus.LOCKED));

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        stats.setNewUsersToday(adminUserRepository.countByCreatedAtBetween(todayStart, todayEnd));

        stats.setTodayTransactionCount(adminTransactionRepository.countByCreatedAtBetween(todayStart, todayEnd));
        stats.setTodaySuccessCount(adminTransactionRepository.countByStatusAndCreatedAtBetween(
                TransactionStatus.SUCCESS, todayStart, todayEnd));
        stats.setTodayFailedCount(adminTransactionRepository.countByStatusAndCreatedAtBetween(
                TransactionStatus.FAILED, todayStart, todayEnd));

        BigDecimal todayAmount = adminTransactionRepository.sumSuccessAmountBetween(todayStart, todayEnd);
        stats.setTodayTransactionAmount(todayAmount != null ? todayAmount : BigDecimal.ZERO);

        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        stats.setMonthTransactionCount(adminTransactionRepository.countByCreatedAtBetween(monthStart, todayEnd));
        BigDecimal monthAmount = adminTransactionRepository.sumSuccessAmountBetween(monthStart, todayEnd);
        stats.setMonthTransactionAmount(monthAmount != null ? monthAmount : BigDecimal.ZERO);

        stats.setDepositAmount(BigDecimal.ZERO);
        stats.setWithdrawalAmount(BigDecimal.ZERO);
        stats.setTransferAmount(BigDecimal.ZERO);

        List<Object[]> typeStats = adminTransactionRepository.countAndSumByTypeAndDateRange(todayStart, todayEnd);
        for (Object[] row : typeStats) {
            TransactionType type = (TransactionType) row[0];
            long count = ((Number) row[1]).longValue();
            BigDecimal amount = (BigDecimal) row[2];
            switch (type) {
                case DEPOSIT -> {
                    stats.setDepositCount(count);
                    stats.setDepositAmount(amount);
                }
                case WITHDRAWAL -> {
                    stats.setWithdrawalCount(count);
                    stats.setWithdrawalAmount(amount);
                }
                case TRANSFER_OUT, TRANSFER_IN -> {
                    stats.setTransferCount(stats.getTransferCount() + count);
                    stats.setTransferAmount(stats.getTransferAmount().add(amount));
                }
                default -> { }
            }
        }

        return stats;
    }

    @Override
    @Transactional
    public void cancelTransaction(Long transactionId) {
        Transaction tx = adminTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException(400, "交易记录不存在，ID: " + transactionId));

        if (tx.getStatus() != TransactionStatus.PENDING) {
            throw new BusinessException(400, "只有PENDING状态的交易可以被撤销，当前状态: " + tx.getStatus());
        }

        tx.setStatus(TransactionStatus.CANCELLED);
        adminTransactionRepository.save(tx);

        log.info("管理员撤销交易: transactionId={}, transactionNo={}", transactionId, tx.getTransactionNo());
    }

    private Specification<Transaction> buildSpecification(TransactionQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String keyword = "%" + request.getKeyword().trim() + "%";
                predicates.add(cb.like(root.get("transactionNo"), keyword));
            }

            if (request.getType() != null) {
                predicates.add(root.get("transactionType").in(mapAdminType(request.getType())));
            }

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getStartTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getStartTime()));
            }

            if (request.getEndTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getEndTime()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<TransactionType> mapAdminType(AdminTransactionType type) {
        return switch (type) {
            case DEPOSIT -> List.of(TransactionType.DEPOSIT);
            case WITHDRAWAL -> List.of(TransactionType.WITHDRAWAL);
            case TRANSFER -> List.of(TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN);
        };
    }
}
