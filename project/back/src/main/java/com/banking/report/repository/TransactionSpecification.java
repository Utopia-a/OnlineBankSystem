package com.banking.report.repository;

import com.bank.transaction.entity.Transaction;
import com.bank.transaction.enums.TransactionStatus;
import com.bank.transaction.enums.TransactionType;
import com.banking.report.dto.TransactionQueryRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    private TransactionSpecification() {}

    public static Specification<Transaction> buildSpec(TransactionQueryRequest req, Long accountId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (accountId != null) {
                predicates.add(cb.or(
                        cb.equal(root.get("fromAccountId"), accountId),
                        cb.equal(root.get("toAccountId"), accountId)
                ));
            }
            if (req.getTransactionType() != null) {
                predicates.add(cb.equal(root.get("transactionType"),
                        TransactionType.valueOf(req.getTransactionType())));
            }
            if (req.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"),
                        TransactionStatus.valueOf(req.getStatus())));
            }
            if (req.getStartTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), req.getStartTime()));
            }
            if (req.getEndTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), req.getEndTime()));
            }
            if (req.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), req.getMinAmount()));
            }
            if (req.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), req.getMaxAmount()));
            }
            if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
                String like = "%" + req.getKeyword().trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("transactionNo"), like),
                        cb.like(root.get("remark"), like)
                ));
            }
            if (query.getResultType() != Long.class) {
                query.orderBy(cb.desc(root.get("createdAt")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
