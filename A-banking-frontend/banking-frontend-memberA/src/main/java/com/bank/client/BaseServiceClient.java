package com.bank.client;

import com.bank.dto.response.ApiResponse;
import com.bank.exception.BackendServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * 后端服务调用基类
 * 封装公共的 HTTP 请求逻辑、统一错误处理、JWT Token 透传
 */
public abstract class BaseServiceClient {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected RestTemplate restTemplate;

    /**
     * 构建带 JWT Token 的请求头
     */
    protected HttpHeaders buildHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null && !token.isBlank()) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    /**
     * GET 请求
     */
    protected <T> T get(String url, String token, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(buildHeaders(token));
            ResponseEntity<ApiResponse<T>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, entity, responseType);
            return extractData(resp.getBody(), url);
        } catch (HttpClientErrorException e) {
            log.error("GET {} 失败: {} {}", url, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BackendServiceException("请求失败: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("GET {} 发生异常: {}", url, e.getMessage());
            throw new BackendServiceException("服务暂时不可用，请稍后重试", e);
        }
    }

    /**
     * POST 请求
     */
    protected <T, R> T post(String url, R body, String token,
                            ParameterizedTypeReference<ApiResponse<T>> responseType) {
        try {
            HttpEntity<R> entity = new HttpEntity<>(body, buildHeaders(token));
            ResponseEntity<ApiResponse<T>> resp = restTemplate.exchange(
                    url, HttpMethod.POST, entity, responseType);
            return extractData(resp.getBody(), url);
        } catch (HttpClientErrorException e) {
            log.error("POST {} 失败: {} {}", url, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BackendServiceException("请求失败: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("POST {} 发生异常: {}", url, e.getMessage());
            throw new BackendServiceException("服务暂时不可用，请稍后重试", e);
        }
    }

    /**
     * PUT 请求
     */
    protected <T, R> T put(String url, R body, String token,
                           ParameterizedTypeReference<ApiResponse<T>> responseType) {
        try {
            HttpEntity<R> entity = new HttpEntity<>(body, buildHeaders(token));
            ResponseEntity<ApiResponse<T>> resp = restTemplate.exchange(
                    url, HttpMethod.PUT, entity, responseType);
            return extractData(resp.getBody(), url);
        } catch (HttpClientErrorException e) {
            log.error("PUT {} 失败: {} {}", url, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BackendServiceException("请求失败: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("PUT {} 发生异常: {}", url, e.getMessage());
            throw new BackendServiceException("服务暂时不可用，请稍后重试", e);
        }
    }

    /**
     * DELETE 请求
     */
    protected <T> T delete(String url, String token,
                           ParameterizedTypeReference<ApiResponse<T>> responseType) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(buildHeaders(token));
            ResponseEntity<ApiResponse<T>> resp = restTemplate.exchange(
                    url, HttpMethod.DELETE, entity, responseType);
            return extractData(resp.getBody(), url);
        } catch (HttpClientErrorException e) {
            log.error("DELETE {} 失败: {} {}", url, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BackendServiceException("请求失败: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("DELETE {} 发生异常: {}", url, e.getMessage());
            throw new BackendServiceException("服务暂时不可用，请稍后重试", e);
        }
    }

    private <T> T extractData(ApiResponse<T> apiResponse, String url) {
        if (apiResponse == null) {
            throw new BackendServiceException("后端服务返回空响应: " + url);
        }
        if (!apiResponse.isSuccess()) {
            throw new BackendServiceException(apiResponse.getMessage());
        }
        return apiResponse.getData();
    }
}
