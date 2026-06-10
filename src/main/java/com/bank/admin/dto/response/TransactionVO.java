package com.bank.admin.dto.response;

import com.bank.transaction.entity.Transaction;
import com.bank.transaction.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionVO {

    private Long id;
    private String transactionNo;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private String type;
    private String status;
    private String remark;
    private LocalDateTime createdAt;

    public static TransactionVO fromEntity(Transaction tx) {
        TransactionVO vo = new TransactionVO();
        vo.setId(tx.getId());
        vo.setTransactionNo(tx.getTransactionNo());
        vo.setFromAccountId(tx.getFromAccountId());
        vo.setToAccountId(tx.getToAccountId());
        vo.setAmount(tx.getAmount());
        vo.setType(formatType(tx.getTransactionType()));
        vo.setStatus(tx.getStatus().name());
        vo.setRemark(tx.getRemark());
        vo.setCreatedAt(tx.getCreatedAt());
        return vo;
    }

    private static String formatType(TransactionType type) {
        if (type == TransactionType.TRANSFER_OUT || type == TransactionType.TRANSFER_IN) {
            return "TRANSFER";
        }
        return type.name();
    }
}
