package com.banking.report;

import com.banking.report.dto.ReportResponse.TransactionDTO;
import com.banking.report.util.ExportUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ExportUtil 单元测试")
class ExportUtilTest {

    private final ExportUtil exportUtil = new ExportUtil();

    private List<TransactionDTO> buildTestData() {
        return List.of(
                TransactionDTO.builder()
                        .id(1L).transactionNo("TXN001")
                        .fromAccountId(1L).fromAccountNo("ACC001")
                        .toAccountId(2L).toAccountNo("ACC002")
                        .amount(BigDecimal.valueOf(1000.50))
                        .balanceBefore(BigDecimal.valueOf(5000))
                        .balanceAfter(BigDecimal.valueOf(3999.50))
                        .transactionType("TRANSFER").transactionTypeLabel("转账")
                        .status("SUCCESS").statusLabel("成功")
                        .remark("测试转账")
                        .createdAt(LocalDateTime.now())
                        .build(),
                TransactionDTO.builder()
                        .id(2L).transactionNo("TXN002")
                        .toAccountId(1L).toAccountNo("ACC001")
                        .amount(BigDecimal.valueOf(500))
                        .transactionType("DEPOSIT").transactionTypeLabel("存款")
                        .status("SUCCESS").statusLabel("成功")
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    @Test
    @DisplayName("Excel 导出 - 输出流非空")
    void writeExcel_producesNonEmptyOutput() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exportUtil.writeExcel(buildTestData(), out, "测试账单 2024-01");

        byte[] bytes = out.toByteArray();
        assertThat(bytes).isNotEmpty();
        // XLSX 文件以 PK 开头（ZIP 格式）
        assertThat(bytes[0]).isEqualTo((byte) 0x50); // 'P'
        assertThat(bytes[1]).isEqualTo((byte) 0x4B); // 'K'
    }

    @Test
    @DisplayName("CSV 导出 - 包含 UTF-8 BOM 和表头")
    void writeCsv_containsBomAndHeaders() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exportUtil.writeCsv(buildTestData(), out);

        byte[] bytes = out.toByteArray();
        // 前 3 字节为 UTF-8 BOM
        assertThat(bytes[0]).isEqualTo((byte) 0xEF);
        assertThat(bytes[1]).isEqualTo((byte) 0xBB);
        assertThat(bytes[2]).isEqualTo((byte) 0xBF);

        String content = new String(bytes, 3, bytes.length - 3, "UTF-8");
        assertThat(content).contains("流水号");
        assertThat(content).contains("交易类型");
        assertThat(content).contains("TXN001");
        assertThat(content).contains("转账");
    }

    @Test
    @DisplayName("空数据集不报错")
    void exportEmptyList_noException() {
        assertThatCode(() -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            exportUtil.writeExcel(List.of(), out, "空账单");
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            exportUtil.writeCsv(List.of(), out);
        }).doesNotThrowAnyException();
    }
}
