package com.bank.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一 API 响应结构
 *
 * @param <T> 响应数据类型
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "统一响应结构")
public class ApiResponse<T> {

    @Schema(description = "业务状态码，200表示成功", example = "200")
    private int code;

    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    private ApiResponse() {}

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.code = 200;
        resp.message = "操作成功";
        resp.data = data;
        return resp;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.code = 200;
        resp.message = message;
        resp.data = data;
        return resp;
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.code = code;
        resp.message = message;
        return resp;
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(500, message);
    }

    public boolean isSuccess() {
        return this.code == 200;
    }
}
