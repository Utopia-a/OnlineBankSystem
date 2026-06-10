package com.banking.report;

import com.banking.report.dto.TransactionQueryRequest;
import com.banking.report.dto.ReportResponse.*;
import com.banking.report.entity.AccountSummary;
import com.banking.report.entity.TransactionRecord;
import com.banking.report.entity.TransactionRecord.TransactionStatus;
import com.banking.report.entity.TransactionRecord.TransactionType;
import com.banking.report.exception.ReportException;
import com.banking.report.repository.AccountRepository;
import com.banking.report.repository.TransactionRepository;
import com.banking.report.service.ReportService;
import com.banking.report.util.ExportUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService 单元测试")
class ReportServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock AccountRepository accountRepository;
    @Mock ExportUtil exportUtil;

    @InjectMocks
    ReportService reportService;

    private AccountSummary mockAccount;
    private TransactionRecord mockTx;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reportService, "maxExportRows", 50000);

        mockAccount = AccountSummary.builder()
                .id(1L).accountNo("ACC001").userId(100L).balance(BigDecimal.valueOf(5000))
                .status("ACTIVE").build();

        mockTx = TransactionRecord.builder()
                .id(1L).transactionNo("TXN20240101001")
                .fromAccountId(1L).toAccountId(2L)
                .amount(BigDecimal.valueOf(500))
                .balanceBefore(BigDecimal.valueOf(5500))
                .balanceAfter(BigDecimal.valueOf(5000))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .remark("测试转账")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ===== 分页查询测试 =====

    @Test
    @DisplayName("分页查询成功")
    void listTransactions_success() {
        when(accountRepository.existsByIdAndUserId(1L, 100L)).thenReturn(true);
        when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mockTx)));
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(mockAccount));

        TransactionQueryRequest req = new TransactionQueryRequest();
        PageResult<TransactionDTO> result = reportService.listTransactions(1L, req, 100L);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getTransactionNo()).isEqualTo("TXN20240101001");
    }

    @Test
    @DisplayName("无权访问他人账户")
    void listTransactions_accessDenied() {
        when(accountRepository.existsByIdAndUserId(1L, 999L)).thenReturn(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccount));

        TransactionQueryRequest req = new TransactionQueryRequest();
        assertThatThrownBy(() -> reportService.listTransactions(1L, req, 999L))
                .isInstanceOf(ReportException.AccessDeniedException.class);
    }

    @Test
    @DisplayName("账户不存在时抛出 404")
    void listTransactions_accountNotFound() {
        when(accountRepository.existsByIdAndUserId(99L, 100L)).thenReturn(false);
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        TransactionQueryRequest req = new TransactionQueryRequest();
        assertThatThrownBy(() -> reportService.listTransactions(99L, req, 100L))
                .isInstanceOf(ReportException.AccountNotFoundException.class);
    }

    // ===== 交易详情测试 =====

    @Test
    @DisplayName("查询交易详情成功")
    void getTransactionDetail_success() {
        when(transactionRepository.findByTransactionNo("TXN20240101001"))
                .thenReturn(Optional.of(mockTx));
        when(accountRepository.existsByIdAndUserId(1L, 100L)).thenReturn(true);
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(mockAccount));

        TransactionDTO dto = reportService.getTransactionDetail("TXN20240101001", 100L);

        assertThat(dto).isNotNull();
        assertThat(dto.getTransactionNo()).isEqualTo("TXN20240101001");
        assertThat(dto.getTransactionTypeLabel()).isEqualTo("转账");
        assertThat(dto.getStatusLabel()).isEqualTo("成功");
    }

    @Test
    @DisplayName("流水号不存在时抛出 404")
    void getTransactionDetail_notFound() {
        when(transactionRepository.findByTransactionNo(anyString())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reportService.getTransactionDetail("INVALID", 100L))
                .isInstanceOf(ReportException.TransactionNotFoundException.class);
    }

    // ===== 统计测试 =====

    @Test
    @DisplayName("收支统计正常返回")
    void getAccountStat_success() {
        LocalDateTime start = LocalDateTime.now().minusMonths(1);
        LocalDateTime end = LocalDateTime.now();

        when(accountRepository.existsByIdAndUserId(1L, 100L)).thenReturn(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccount));
        when(transactionRepository.sumIncome(eq(1L), any(), any())).thenReturn(BigDecimal.valueOf(2000));
        when(transactionRepository.sumExpense(eq(1L), any(), any())).thenReturn(BigDecimal.valueOf(500));
        when(transactionRepository.groupByTypeStats(eq(1L), any(), any())).thenReturn(List.of());
        when(transactionRepository.groupByDayStats(eq(1L), any(), any())).thenReturn(List.of());

        AccountStatDTO stat = reportService.getAccountStat(1L, start, end, 100L);

        assertThat(stat.getTotalIncome()).isEqualByComparingTo("2000");
        assertThat(stat.getTotalExpense()).isEqualByComparingTo("500");
        assertThat(stat.getNetAmount()).isEqualByComparingTo("1500");
    }

    @Test
    @DisplayName("时间范围非法（开始晚于结束）抛出异常")
    void getAccountStat_invalidDateRange() {
        when(accountRepository.existsByIdAndUserId(1L, 100L)).thenReturn(true);

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().minusDays(1); // 结束早于开始

        assertThatThrownBy(() -> reportService.getAccountStat(1L, start, end, 100L))
                .isInstanceOf(ReportException.InvalidQueryException.class)
                .hasMessageContaining("开始时间不能晚于结束时间");
    }

    @Test
    @DisplayName("时间范围超过 1 年抛出异常")
    void getAccountStat_dateRangeTooLong() {
        when(accountRepository.existsByIdAndUserId(1L, 100L)).thenReturn(true);

        LocalDateTime start = LocalDateTime.now().minusYears(2);
        LocalDateTime end = LocalDateTime.now();

        assertThatThrownBy(() -> reportService.getAccountStat(1L, start, end, 100L))
                .isInstanceOf(ReportException.InvalidQueryException.class)
                .hasMessageContaining("1 年");
    }
}
