package com.bank.client;

import com.bank.dto.response.ApiResponse;
import com.bank.dto.response.PageResponse;
import com.bank.dto.response.TransactionResponse;
import com.bank.exception.BackendServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 账单&报表服务客户端（对接成员E - 账单 & 报表服务）
 * 端口：8084
 */
@Component
public class BillServiceClient extends BaseServiceClient {

    @Value("${backend.bill-service.url}")
    private String billServiceUrl;

    /**
     * 分页查询交易历史
     *
     * @param accountNo 账户号
     * @param page      页码（从1开始）
     * @param pageSize  每页大小
     * @param startDate 开始日期（yyyy-MM-dd，可选）
     * @param endDate   结束日期（yyyy-MM-dd，可选）
     * @param token     JWT Token
     */
    public PageResponse<TransactionResponse> getTransactionHistory(
            String accountNo, int page, int pageSize,
            String startDate, String endDate, String token) {

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(billServiceUrl + "/api/bills/history")
                .queryParam("accountNo", accountNo)
                .queryParam("page", page)
                .queryParam("pageSize", pageSize);

        if (startDate != null && !startDate.isBlank()) {
            builder.queryParam("startDate", startDate);
        }
        if (endDate != null && !endDate.isBlank()) {
            builder.queryParam("endDate", endDate);
        }

        String url = builder.toUriString();
        log.info("查询交易历史: 账户 {}, 第{}页", accountNo, page);
        return get(url, token,
                new ParameterizedTypeReference<ApiResponse<PageResponse<TransactionResponse>>>() {});
    }

    /**
     * 导出交易记录（返回文件字节数组）
     * 注意：导出接口直接返回文件流，需特殊处理
     */
    public byte[] exportTransactions(String accountNo, String startDate, String endDate,
                                     String format, String token) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(billServiceUrl + "/api/bills/export")
                .queryParam("accountNo", accountNo)
                .queryParam("format", format); // CSV 或 PDF

        if (startDate != null) builder.queryParam("startDate", startDate);
        if (endDate != null) builder.queryParam("endDate", endDate);

        String url = builder.toUriString();
        log.info("导出交易记录: 账户 {}, 格式 {}", accountNo, format);

        try {
            HttpHeaders headers = buildHeaders(token);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_OCTET_STREAM));
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> resp = restTemplate.exchange(
                    url, HttpMethod.GET, entity, byte[].class);
            return resp.getBody();
        } catch (Exception e) {
            log.error("导出交易记录失败: {}", e.getMessage());
            throw new BackendServiceException("导出失败，请稍后重试", e);
        }
    }
}
