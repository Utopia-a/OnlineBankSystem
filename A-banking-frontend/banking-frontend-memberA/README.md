# 成员A - 前端 & 集成模块

## 模块概述

本模块是网上银行系统的**前端展示层与 API 集成层**，负责：

- 用户界面（Thymeleaf + 原生 JS）
- 向其他成员的后端服务发起 HTTP 请求（API 代理）
- Swagger / OpenAPI 3.0 接口文档

---

## 技术栈

| 组件 | 版本 |
|------|------|
| JDK | 17 |
| Spring Boot | 3.2.5 |
| Thymeleaf | 3.x（随 Boot） |
| SpringDoc OpenAPI | 2.5.0 |
| Apache HttpClient5 | 5.x（随 Boot） |
| Maven | 3.8+ |

---

## 目录结构

```
banking-frontend/
├── pom.xml
└── src/main/
    ├── java/com/bank/
    │   ├── BankingFrontendApplication.java   # 启动类
    │   ├── config/
    │   │   ├── SwaggerConfig.java            # Swagger/OpenAPI 配置
    │   │   ├── RestTemplateConfig.java       # HTTP 客户端配置
    │   │   └── WebMvcConfig.java             # MVC/CORS/拦截器配置
    │   ├── interceptor/
    │   │   └── JwtTokenInterceptor.java      # JWT 请求拦截器
    │   ├── controller/
    │   │   ├── PageController.java           # 页面路由（HTML 渲染）
    │   │   ├── AuthController.java           # 认证 API（对接成员B）
    │   │   ├── AccountController.java        # 账户 API（对接成员C）
    │   │   ├── TransactionController.java    # 交易 API（对接成员D）
    │   │   └── BillController.java           # 账单 API（对接成员E）
    │   ├── client/
    │   │   ├── BaseServiceClient.java        # 请求基类
    │   │   ├── AuthServiceClient.java        # 调用成员B
    │   │   ├── AccountServiceClient.java     # 调用成员C
    │   │   ├── TransactionServiceClient.java # 调用成员D
    │   │   └── BillServiceClient.java        # 调用成员E
    │   ├── dto/
    │   │   ├── request/                      # 请求 DTO
    │   │   └── response/                     # 响应 DTO
    │   └── exception/
    │       ├── BackendServiceException.java  # 自定义异常
    │       └── GlobalExceptionHandler.java   # 全局异常处理
    └── resources/
        ├── application.yml                   # 配置文件
        ├── templates/                        # Thymeleaf 模板
        │   ├── login.html
        │   ├── register.html
        │   ├── dashboard.html
        │   ├── accounts.html
        │   ├── transfer.html
        │   ├── transactions.html
        │   ├── profile.html
        │   └── fragments/nav.html
        └── static/
            ├── css/common.css
            └── js/
                ├── api.js                    # Fetch 封装
                └── app.js                    # 工具函数
```

---

## 快速启动

### 1. 修改后端服务地址

编辑 `src/main/resources/application.yml`：

```yaml
backend:
  auth-service:
    url: http://localhost:8081   # 改为成员B的实际地址
  account-service:
    url: http://localhost:8082   # 改为成员C的实际地址
  transaction-service:
    url: http://localhost:8083   # 改为成员D的实际地址
  bill-service:
    url: http://localhost:8084   # 改为成员E的实际地址
```

### 2. 编译运行

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar target/banking-frontend-1.0.0.jar

# 或直接运行
mvn spring-boot:run
```

### 3. 访问地址

| 地址 | 说明 |
|------|------|
| http://localhost:8080/login | 登录页面 |
| http://localhost:8080/register | 注册页面 |
| http://localhost:8080/dashboard | 首页（需登录） |
| http://localhost:8080/swagger-ui.html | **Swagger 接口文档** |
| http://localhost:8080/v3/api-docs | OpenAPI JSON |

---

## API 接口一览

### 认证模块（不需 Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/refresh` | 刷新 Token |

### 认证模块（需 Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/otp/send` | 发送 OTP 验证码 |
| POST | `/api/auth/otp/verify` | 验证 OTP |
| POST | `/api/auth/logout` | 退出登录 |

### 账户模块（需 Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/accounts/my` | 查询我的账户 |
| GET | `/api/accounts/{accountNo}` | 账户详情 |
| GET | `/api/accounts/{accountNo}/balance` | 余额查询 |
| POST | `/api/accounts` | 开户 |

### 交易模块（需 Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/transactions/transfer` | 转账 |
| POST | `/api/transactions/deposit` | 存款 |
| POST | `/api/transactions/withdraw` | 取款 |
| GET | `/api/transactions/{id}` | 交易详情 |

### 账单模块（需 Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/bills/history` | 分页查询交易历史 |
| GET | `/api/bills/export` | 导出（CSV/PDF） |

---

## 与其他成员的接口约定

本模块作为 **API 网关**，请求链路为：

```
浏览器 → 成员A (8080) → 成员B/C/D/E/F 各服务
```

**各成员需遵守的响应格式：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

**JWT Token 传递方式：**  
成员A在所有转发请求的 `Authorization` Header 中携带原始 Token：
```
Authorization: Bearer <jwt_token>
```

---

## 注意事项

1. **无 Redis 缓存**：Token 失效检测依赖成员B的认证服务
2. **编码**：全项目 UTF-8，换行符 LF
3. **跨域**：已在 `WebMvcConfig` 配置 `/api/**` 的 CORS，前端直接调用无需额外配置
4. **Swagger 文档**：所有 Controller 均有完整注解，可直接在 Swagger UI 中测试接口
