package com.bank.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SystemConfigRequest {

    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过100")
    private String configKey;

    @NotBlank(message = "配置值不能为空")
    private String configValue;

    @Size(max = 255, message = "描述长度不能超过255")
    private String description;
}
