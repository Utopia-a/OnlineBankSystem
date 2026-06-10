package com.banking.report.repository;

import com.banking.report.dto.TransactionQueryRequest;
import com.banking.report.entity.TransactionRecord;
import com.banking.report.entity.TransactionRecord.TransactionStatus;
import com.banking.report.entity.TransactionRecord.TransactionType;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * 交易记录动态查询条件构造器（JPA Specification）
 * 支持按账户ID、交易类型、状态、金额范围、时间范围、关键词等多条件组合查询
 */
public class TransactionSpecification {

    private TransactionSpecification() {}

    public static Specification<TransactionRecord> buildSpec(
            TransactionQueryRequest req, Long accountId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 账户条件：from 或 to
            if (accountId != null) {
                Predicate fromAcc = cb.equal(root.get("fromAccountId"), accountId);
                Predicate toAcc   = cb.equal(root.get("toAccountId"),   accountId);
                predicates.add(cb.or(fromAcc, toAcc));
            }

            // 交易类型
            if (req.getTransactionType() != null) {
                predicates.add(cb.equal(root.get("transactionType"),
                        TransactionType.valueOf(req.getTransactionType())));
            }

            // 交易状态
            if (req.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"),
                        TransactionStatus.valueOf(req.getStatus())));
            }

            // 时间范围
            if (req.getStartTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), req.getStartTime()));
            }
            if (req.getEndTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), req.getEndTime()));
            }

            // 金额范围
            if (req.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), req.getMinAmount()));
            }
            if (req.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), req.getMaxAmount()));
            }

            // 关键词（模糊匹配流水号或备注）
            if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
                String like = "%" + req.getKeyword().trim() + "%";
                Predicate noLike     = cb.like(root.get("transactionNo"), like);
                Predicate remarkLike = cb.like(root.get("remark"), like);
                predicates.add(cb.or(noLike, remarkLike));
            }

            // 默认排序：createAt DESC（分页时由 Pageable 控制，这里兜底）
            if (query.getResultType() != Long.class) {
                query.orderBy(cb.desc(root.get("createdAt")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
