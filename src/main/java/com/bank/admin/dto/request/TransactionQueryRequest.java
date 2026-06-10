package com.bank.admin.dto.request;

import com.bank.admin.enums.AdminTransactionType;
import com.bank.transaction.enums.TransactionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransactionQueryRequest extends PageRequest {

    private String keyword;
    private AdminTransactionType type;
    private TransactionStatus status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
