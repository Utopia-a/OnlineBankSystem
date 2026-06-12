# 网上银行系统 — 10 人代码分配 & 模块文件目录对照表

> 项目根目录：`OnlineBankSystem/`  
> 工程代码位于 `project/front`（前端）与 `project/back`（后端）  
> **Excel 分配表：** [.doc/CODE_ALLOCATION.xlsx](./CODE_ALLOCATION.xlsx)  
> 更新日期：2026-06-11

---

## 一、分配原则

| 类别 | 人数 | 工作量 | 说明 |
|------|------|--------|------|
| **原核心成员** | A ~ F（6 人） | **约 72%**（~120 个文件） | 各负责完整业务模块，代码量较多 |
| **新增成员** | G ~ J（4 人） | **约 28%**（~47 个文件） | 从原模块拆分出的集成、页面、基建、文档任务，代码量较少 |

---

## 二、成员总览

| 成员 | 类型 | 角色 | 职责概述 | 文件数 |
|------|------|------|----------|--------|
| **A** | 原核心 | 前端负责人 | 认证页面、全局样式/JS、页面路由、Swagger | 14 |
| **B** | 原核心 | 认证安全工程师 | 注册/登录、JWT、Security、OTP | 23 |
| **C** | 原核心 | 账户模块开发 | 账户 CRUD、余额、状态管理 | 22 |
| **D** | 原核心 | 交易模块开发 | 转账、存款、取款、事务控制 | 21 |
| **E** | 原核心 | 报表模块开发 | 交易历史、统计、导出 | 10 |
| **F** | 原核心 | 管理员后台开发 | 用户管理、交易监控、系统配置 | 28 |
| **G** | 新增 | API 集成工程师 | Web API 控制器、前端 DTO、全局异常 | 15 |
| **H** | 新增 | 业务前端开发 | 账户/转账/交易/个人中心页面 | 4 |
| **I** | 新增 | 基础设施工程师 | 启动类、Maven、Docker、全局配置 | 10 |
| **J** | 新增 | 数据库与项目文档 | schema.sql 统筹、模块文档维护 | 6 |

---

## 三、项目总览

```
OnlineBankSystem/
├── .doc/                          # 【J】项目文档 & 代码分配表
├── pom.xml                        # 【I】Maven 聚合工程
├── README.md                      # 【I】项目说明
└── project/
    ├── front/                     # 【A + H】前端页面与静态资源
    │   ├── templates/
    │   └── static/
    └── back/                      # 后端 Spring Boot 工程
        ├── pom.xml                # 【I】
        ├── src/main/java/
        ├── src/main/resources/    # 【I 主维护，各模块维护对应节】
        ├── src/test/java/         # 各模块负责人维护
        ├── sql/                   # 【J 主维护，B/C/D/F 协同】
        ├── docker-compose.yml     # 【I】
        └── scripts/               # 【I】
```

---

## 四、成员 A — 前端负责人（原核心）

**职责：** 认证相关页面、全局样式与 JS、Thymeleaf 路由、Swagger 文档配置

| 路径 | 说明 |
|------|------|
| `project/front/templates/login.html` | 登录页 |
| `project/front/templates/register.html` | 注册页（含邮箱验证步骤） |
| `project/front/templates/verify-email.html` | 邮箱验证页 |
| `project/front/templates/dashboard.html` | 首页 / 账户概览 |
| `project/front/templates/fragments/nav.html` | 公共导航栏片段 |
| `project/front/static/css/common.css` | 全局样式（设计系统） |
| `project/front/static/js/api.js` | HTTP 请求封装（JWT、错误处理） |
| `project/front/static/js/app.js` | 公共工具（格式化金额、日期、Toast） |
| `project/front/static/js/auth-verify.js` | 邮箱验证相关 API |
| `project/front/README.md` | 前端目录说明 |
| `project/back/.../PageController.java` | Thymeleaf 页面路由 |
| `project/back/.../WebMvcConfig.java` | 静态资源、CORS、首页重定向 |
| `project/back/.../OpenApiConfig.java` | Swagger / OpenAPI 配置 |
| `application.yml`（springdoc.* 节） | Swagger 路径配置（协同 I） |

---

## 五、成员 B — 认证安全工程师（原核心）

**职责：** 注册/登录、JWT、Spring Security、OTP 邮箱验证

```
project/back/src/main/java/com/banking/auth/
├── dto/          AuthRequest.java, AuthResponse.java
├── entity/       User.java, RefreshToken.java, OtpRecord.java
├── exception/    AuthException.java
├── filter/       JwtAuthenticationFilter.java
├── repository/   UserRepository, RefreshTokenRepository, OtpRecordRepository
├── service/      AuthService, OtpService, RefreshTokenService, CustomUserDetailsService
└── util/         JwtUtil.java, OtpUtil.java
```

| 关联文件 | 说明 |
|---------|------|
| `SecurityConfig.java` | Spring Security 白名单与过滤器链 |
| `BankUserDetails.java` | UserDetails 扩展（协同 C） |
| `schema.sql`（users/otp/refresh_tokens） | 协同 J |
| `AuthServiceTest.java` / `JwtUtilTest.java` / `AdminPasswordHashTest.java` | 单元测试 |

---

## 六、成员 C — 账户模块开发（原核心）

**职责：** 账户 CRUD、余额查询、账户状态管理

```
project/back/src/main/java/com/bank/account/
├── config/       AccountNumberGenerator.java
├── controller/   AdminAccountController, InternalAccountController
├── dto/          6 个请求/响应 DTO
├── entity/       Account.java
├── enums/        AccountType, AccountStatus
├── exception/    4 个账户异常
├── repository/   AccountRepository.java
└── service/      AccountService, AccountServiceImpl
```

| 关联 | 说明 |
|------|------|
| `schema.sql`（accounts 表） | 协同 J |
| `AccountServiceTest.java` | 单元测试 |

---

## 七、成员 D — 交易模块开发（原核心）

**职责：** 转账、存款、取款、事务控制

```
project/back/src/main/java/com/bank/transaction/
├── config/       TransactionNoGenerator, TransactionProperties
├── dto/          5 个请求/响应 DTO
├── entity/       Transaction.java
├── enums/        TransactionType, TransactionStatus
├── exception/    5 个交易异常（含 BusinessException）
├── repository/   TransactionRepository.java
└── service/      TransactionService, TransactionServiceImpl
```

| 关联 | 说明 |
|------|------|
| `application.yml`（transaction.* 节） | 协同 I |
| `schema.sql`（transactions 表） | 协同 J |
| `TransactionServiceTest.java` | 单元测试 |

---

## 八、成员 E — 报表模块开发（原核心）

**职责：** 交易历史查询、分页、统计、CSV/Excel/PDF 导出

```
project/back/src/main/java/com/banking/report/
├── dto/          ReportResponse.java, TransactionQueryRequest.java
├── exception/    ReportException.java
├── repository/   TransactionSpecification.java
├── service/      ReportService.java, UserContext.java
└── util/         ExportUtil.java, PageUtil.java
```

| 关联 | 说明 |
|------|------|
| `application.yml`（export.* 节） | 协同 I |
| `ExportUtilTest.java` | 单元测试 |

---

## 九、成员 F — 管理员后台开发（原核心）

**职责：** 用户管理、交易监控、系统配置

```
project/back/src/main/java/com/bank/admin/
├── controller/   AdminUserController, TransactionMonitorController, SystemConfigController
├── dto/          5 个 request + 5 个 response
├── entity/       SystemConfig.java
├── enums/        AdminTransactionType.java
├── repository/   AdminUserRepository, AdminTransactionRepository, SystemConfigRepository
└── service/      3 个接口 + 3 个 impl
```

| 关联 | 说明 |
|------|------|
| `schema.sql`（system_config + admin 账号） | 协同 J |
| `fix-admin-password.sql` | 管理员密码修复 |
| `AdminUserServiceTest.java` / `SystemConfigServiceTest.java` | 单元测试 |

---

## 十、成员 G — API 集成工程师（新增）

**职责：** Web API 控制器、前端 DTO 适配、全局异常处理

| 文件 | 对外路径 | 说明 |
|------|---------|------|
| `WebAuthController.java` | `/api/auth/**` | 登录、注册、邮箱验证、Token |
| `WebAccountController.java` | `/api/accounts/**` | 账户查询、开户 |
| `WebTransactionController.java` | `/api/transactions/**` | 转账、存款、取款 |
| `WebBillController.java` | `/api/bills/**` | 交易历史、导出 |
| `com/bank/dto/request/*`（4 个） | — | 前端请求 DTO |
| `com/bank/dto/response/*`（6 个） | — | 前端响应 DTO |
| `GlobalExceptionHandler.java` | — | 全局异常处理 |

---

## 十一、成员 H — 业务前端开发（新增）

**职责：** 账户、转账、交易记录、个人中心等业务页面

| 路径 | 说明 |
|------|------|
| `project/front/templates/accounts.html` | 账户管理页 |
| `project/front/templates/transfer.html` | 转账汇款页 |
| `project/front/templates/transactions.html` | 交易记录页（含导出） |
| `project/front/templates/profile.html` | 个人中心页 |

---

## 十二、成员 I — 基础设施工程师（新增）

**职责：** 工程构建、启动类、运行环境、全局配置统筹

| 文件 / 目录 | 说明 |
|------------|------|
| `OnlineBankApplication.java` | Spring Boot 启动类 |
| `project/back/pom.xml` | 后端 Maven（含 front 资源打包） |
| `pom.xml`（根目录） | Maven 聚合工程 |
| `application.yml` | 数据库、端口等全局配置（**主维护**） |
| `docker-compose.yml` | MySQL 本地环境 |
| `scripts/start.bat` / `build-all.bat` | 启动与编译脚本 |
| `README.md` | 项目说明 |
| `.doc/SETUP.md` | 部署与 IDEA 配置 |
| `.gitignore` | Git 忽略规则 |

---

## 十三、成员 J — 数据库与项目文档（新增）

**职责：** 数据库脚本统筹、模块文档、代码分配文档维护

| 文件 | 说明 |
|------|------|
| `project/back/sql/schema.sql` | 统一数据库初始化（**主维护**，B/C/D/F 协同各表段） |
| `.doc/MODULE_FILES.md` | 本文档 |
| `.doc/CODE_ALLOCATION.xlsx` | 10 人代码分配 Excel 表 |
| `.doc/成员B-认证安全模块文档.docx` | 成员 B 模块文档 |
| `.doc/成员C-账户管理模块文档.docx` | 成员 C 模块文档 |
| `.doc/成员E-账单报表模块文档.docx` | 成员 E 模块文档 |

---

## 十四、模块依赖关系

```
┌──────────────────────────────────────────────────────────────┐
│  A：认证前端 + 样式    H：业务前端页面                          │
└──────────────────────────┬───────────────────────────────────┘
                           │ 页面调用
┌──────────────────────────▼───────────────────────────────────┐
│  G：Web*Controller + 前端 DTO + GlobalExceptionHandler        │
└──────────────────────────┬───────────────────────────────────┘
                           │ 调用 Service
     ┌─────────────────────┼─────────────────────┐
     ▼                     ▼                     ▼
┌─────────┐          ┌─────────┐          ┌─────────┐
│ B 认证  │◄────────►│ C 账户  │◄────────►│ D 交易  │
└─────────┘          └─────────┘          └────┬────┘
                                               │ 只读
                                          ┌────▼────┐
                                          │ E 报表  │
                                          └─────────┘
     ┌──────────────────────────────────────────────┐
     │  F：管理员后台（读写 B/C/D + 自有 system_config） │
     └──────────────────────────────────────────────┘
     ┌──────────────────────────────────────────────┐
     │  I：基础设施（启动/构建/配置）  J：数据库/文档    │
     └──────────────────────────────────────────────┘
```

---

## 十五、快速定位指南

| 我想改… | 负责人 | 去哪个目录 |
|--------|--------|-----------|
| 登录/注册页面 | A | `project/front/templates/` |
| 转账/账户页面 | H | `project/front/templates/` |
| 全局 CSS 样式 | A | `project/front/static/css/` |
| 前端调 API 逻辑 | A | `project/front/static/js/api.js` |
| Web API 接口层 | G | `com/bank/web/Web*Controller.java` |
| 用户登录业务逻辑 | B | `com/banking/auth/service/AuthService.java` |
| JWT / 权限白名单 | B | `com/banking/config/SecurityConfig.java` |
| 开户 / 查余额 | C | `com/bank/account/service/` |
| 转账逻辑 | D | `com/bank/transaction/service/TransactionServiceImpl.java` |
| 交易记录导出 | E | `com/banking/report/util/ExportUtil.java` |
| 管理员查用户 | F | `com/bank/admin/service/impl/AdminUserServiceImpl.java` |
| 启动 / Maven / Docker | I | `pom.xml`、`docker-compose.yml` |
| 数据库表结构 | J | `project/back/sql/schema.sql` |
| 代码分工查询 | J | `.doc/CODE_ALLOCATION.xlsx` |

---

## 十六、协同维护文件说明

以下文件由多人协同，**主维护人**负责合并冲突：

| 文件 | 主维护 | 协同维护 |
|------|--------|---------|
| `application.yml` | I | A（springdoc）、B（jwt/otp）、D（transaction）、E（export） |
| `schema.sql` | J | B（users/otp）、C（accounts）、D（transactions）、F（system_config/admin） |
| `BankUserDetails.java` | B | C（账户模块引用） |
