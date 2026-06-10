package com.banking.report.util;

import com.banking.report.dto.ReportResponse.TransactionDTO;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 导出工具类
 * 支持 Excel（.xlsx，SXSSF 流式写入防止大数据 OOM）和 CSV
 */
@Component
@Slf4j
public class ExportUtil {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] HEADERS = {
            "流水号", "交易类型", "付款账户ID", "收款账户ID",
            "交易金额", "交易前余额", "交易后余额",
            "交易状态", "备注", "交易时间"
    };

    // ===== Excel 导出（SXSSF 流式，支持大数据量） =====

    /**
     * 将交易列表写入 Excel 并输出到 OutputStream
     *
     * @param records    交易记录列表
     * @param out        输出流（HttpServletResponse.getOutputStream()）
     * @param sheetTitle Sheet 标题（如账单明细 2024-01）
     */
    public void writeExcel(List<TransactionDTO> records, OutputStream out, String sheetTitle)
            throws IOException {
        // SXSSF：内存中只保留最近 500 行，超出自动刷到磁盘
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(500)) {
            workbook.setCompressTempFiles(true);

            Sheet sheet = workbook.createSheet(sheetTitle);
            sheet.setDefaultColumnWidth(18);

            // 样式
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle amountStyle = createAmountStyle(workbook);

            int rowNum = 0;

            // 第一行：大标题
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.setHeightInPoints(28);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(sheetTitle);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            // 第二行：列头
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(20);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            for (TransactionDTO tx : records) {
                Row row = sheet.createRow(rowNum++);
                row.setHeightInPoints(16);
                fillDataRow(row, tx, dataStyle, amountStyle);
            }

            // 汇总行
            Row sumRow = sheet.createRow(rowNum);
            sumRow.setHeightInPoints(18);
            Cell sumLabel = sumRow.createCell(0);
            sumLabel.setCellValue("共 " + records.size() + " 笔记录");
            sumLabel.setCellStyle(headerStyle);

            workbook.write(out);
            workbook.dispose(); // 清理临时文件
        }
        log.debug("Excel 导出完成，共 {} 行", records.size());
    }

    private void fillDataRow(Row row, TransactionDTO tx,
                              CellStyle dataStyle, CellStyle amountStyle) {
        int col = 0;
        setCell(row, col++, tx.getTransactionNo(), dataStyle);
        setCell(row, col++, tx.getTransactionTypeLabel(), dataStyle);
        setCell(row, col++, tx.getFromAccountId() != null ? tx.getFromAccountId().toString() : "-", dataStyle);
        setCell(row, col++, tx.getToAccountId()   != null ? tx.getToAccountId().toString()   : "-", dataStyle);
        setNumericCell(row, col++, tx.getAmount()        != null ? tx.getAmount().doubleValue()        : 0, amountStyle);
        setNumericCell(row, col++, tx.getBalanceBefore() != null ? tx.getBalanceBefore().doubleValue() : 0, amountStyle);
        setNumericCell(row, col++, tx.getBalanceAfter()  != null ? tx.getBalanceAfter().doubleValue()  : 0, amountStyle);
        setCell(row, col++, tx.getStatusLabel(), dataStyle);
        setCell(row, col++, tx.getRemark() != null ? tx.getRemark() : "", dataStyle);
        setCell(row, col, tx.getCreatedAt() != null ? tx.getCreatedAt().format(DT_FMT) : "", dataStyle);
    }

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void setNumericCell(Row row, int col, double value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(style);
        return style;
    }

    private CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style);
        return style;
    }

    private CellStyle createAmountStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("#,##0.00"));
        setBorder(style);
        return style;
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    // ===== CSV 导出 =====

    /**
     * 将交易列表写入 CSV 并输出到 OutputStream（UTF-8 with BOM，兼容 Excel 直接打开）
     */
    public void writeCsv(List<TransactionDTO> records, OutputStream out) throws IOException {
        // UTF-8 BOM，让 Excel 正确识别中文
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        try (Writer writer = new OutputStreamWriter(out, "UTF-8");
             ICSVWriter csvWriter = new CSVWriterBuilder(writer)
                     .withSeparator(',')
                     .withQuoteChar('"')
                     .withLineEnd("\r\n")
                     .build()) {

            // 列头
            csvWriter.writeNext(HEADERS);

            // 数据行
            for (TransactionDTO tx : records) {
                csvWriter.writeNext(new String[]{
                        tx.getTransactionNo(),
                        tx.getTransactionTypeLabel(),
                        tx.getFromAccountId() != null ? tx.getFromAccountId().toString() : "",
                        tx.getToAccountId()   != null ? tx.getToAccountId().toString()   : "",
                        tx.getAmount()        != null ? tx.getAmount().toPlainString()        : "",
                        tx.getBalanceBefore() != null ? tx.getBalanceBefore().toPlainString() : "",
                        tx.getBalanceAfter()  != null ? tx.getBalanceAfter().toPlainString()  : "",
                        tx.getStatusLabel(),
                        tx.getRemark() != null ? tx.getRemark() : "",
                        tx.getCreatedAt() != null ? tx.getCreatedAt().format(DT_FMT) : ""
                });
            }
            csvWriter.flush();
        }
        log.debug("CSV 导出完成，共 {} 行", records.size());
    }
}
