package com.bank.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger / OpenAPI 3.0 接口文档配置
 * 访问地址：http://localhost:8080/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("本地开发环境"),
                        new Server()
                                .url("http://api.bank.example.com")
                                .description("生产环境")
                ))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .name("BearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请在此处填入登录后获取的 JWT Token，格式：Bearer {token}")
                        )
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("网上银行系统 API 接口文档")
                .description("""
                        ## 系统简介
                        本系统为学生项目《网上银行系统》的 RESTful API 文档。
                        
                        ## 模块说明
                        | 模块 | 负责人 | 端口 |
                        |------|--------|------|
                        | 前端 & 集成 | 成员 A/G | 8080 |
                        | 认证 & 安全 | 成员 B | 8081 |
                        | 账户管理 | 成员 C | 8082 |
                        | 交易服务 | 成员 D | 8083 |
                        | 账单 & 报表 | 成员 E | 8084 |
                        | 管理员后台 | 成员 F | 8085 |
                        
                        ## 认证方式
                        接口采用 **JWT Bearer Token** 认证，登录成功后将 token 填入右上角 Authorize。
                        
                        ## 状态码说明
                        - `200` 请求成功
                        - `400` 请求参数错误
                        - `401` 未认证或 Token 过期
                        - `403` 无权限访问
                        - `404` 资源不存在
                        - `500` 服务器内部错误
                        """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("成员 A")
                        .email("memberA@bank.edu")
                )
                .license(new License()
                        .name("学生项目，仅供学习使用")
                );
    }
}
