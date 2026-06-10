<<<<<<< HEAD
# 网上银行系统 需求分析书

**Software Requirements Specification**

技术栈：Spring Boot・Spring Security・MySQL・Redis・JWT

| 文件版本 |       v1.0       |
| :------: | :--------------: |
| 文件状态 |       草稿       |
| 编写日期 |   2026 年 6 月   |
| 适用范围 | 学生小组课程项目 |

## 1. 引言

### 1.1 编写目的

本文档是网上银行系统（Online Banking System）的软件需求分析说明书，旨在明确系统的功能需求、非功能需求、业务规则及约束条件，为小组成员的后续设计、开发与测试工作提供统一依据。

### 1.2 项目背景

本项目为课程小组实践作业，以模拟真实银行业务场景为目标，基于 Spring Boot 框架构建一套具备基本金融功能的网上银行平台。系统面向普通个人用户和银行管理员两类角色，提供账户管理、资金转账、账单查询等核心金融服务。

### 1.3 术语与缩写

| 术语 / 缩写 |                          说明                           |
| :---------: | :-----------------------------------------------------: |
|     SRS     | Software Requirements Specification，软件需求规格说明书 |
|     JWT     |       JSON Web Token，用于用户身份认证的令牌标准        |
|     API     |   Application Programming Interface，应用程序编程接口   |
|     OTP     |     One-Time Password，一次性验证码（用于安全验证）     |
|    RBAC     |      Role-Based Access Control，基于角色的访问控制      |
|     UC      |                     Use Case，用例                      |

### 1.4 参考资料

- Spring Boot 官方文档：[https://spring.io/projects/spring-boot](https://link.wtturl.cn/?target=https%3A%2F%2Fspring.io%2Fprojects%2Fspring-boot&scene=im&aid=497858&lang=zh)
- Spring Security 官方文档：[https://spring.io/projects/spring-security](https://link.wtturl.cn/?target=https%3A%2F%2Fspring.io%2Fprojects%2Fspring-security&scene=im&aid=497858&lang=zh)
- MySQL 8.0 参考手册
- IEEE 830-1998 软件需求规格说明书标准

## 2. 总体描述

### 2.1 产品概述

网上银行系统是一个基于 B/S 架构的 Web 应用程序，后端采用 Spring Boot + Spring Security，前端可使用 Thymeleaf 或 Vue.js，数据库使用 MySQL，缓存使用 Redis。系统为用户提供全天候的在线金融服务，并为管理员提供后台管理能力。

### 2.2 产品功能概览

系统主要功能模块如下：

- 用户认证与授权模块（注册、登录、JWT 鉴权）
- 账户管理模块（开户、查询、冻结 / 解冻）
- 交易服务模块（转账、存款、取款）
- 账单与报表模块（交易记录查询、导出）
- 安全验证模块（OTP 验证码、操作限额）
- 管理员后台模块（用户管理、交易监控、系统配置）

### 2.3 用户类型与特征

|    角色    |     类型     |                      特征描述                      |
| :--------: | :----------: | :------------------------------------------------: |
|  普通用户  |  已注册客户  | 使用网上银行进行日常金融操作，具备基本网络使用能力 |
| 银行管理员 | 内部工作人员 |   负责账户审核、风控管理、系统配置等后台运营工作   |

### 2.4 运行环境

- 后端：JDK 17+，Spring Boot 3.x，Maven 3.8+
- 数据库：MySQL 8.0+，Redis 6.x+
- 服务器：本地开发环境 / Linux 云服务器（Tomcat 内嵌）
- 客户端：现代浏览器（Chrome、Firefox、Edge）

### 2.5 设计与实现约束

- 使用 Spring Boot 作为核心框架，不得替换为其他框架
- 所有 API 接口遵循 RESTful 设计风格，统一返回 JSON 格式数据
- 敏感数据（密码、身份证号）必须加密存储，密码使用 BCrypt 哈希
- 所有金融交易操作必须在数据库事务（`@Transactional`）内完成
- 接口权限控制基于 Spring Security + JWT 实现

## 3. 功能需求

### 3.1 用户认证模块

#### 3.1.1 用户注册

用户提交个人信息完成账号注册，系统自动创建关联银行账户。

|   字段   |                             说明                             |
| :------: | :----------------------------------------------------------: |
|   输入   |     用户名、密码、确认密码、姓名、身份证号、手机号、邮箱     |
|   处理   | 校验字段格式与唯一性；BCrypt 加密密码；生成 16 位账户号；创建账户记录（初始余额 0.00） |
|   输出   |     注册成功提示及分配的账户号；或字段校验失败的错误信息     |
| 业务规则 |    用户名与手机号全局唯一；密码至少 8 位且包含字母与数字     |

#### 3.1.2 用户登录

用户使用用户名与密码登录系统，服务端签发 JWT Token。

- 成功登录返回 accessToken（有效期 30 分钟）与 refreshToken（有效期 7 天）
- 连续登录失败 5 次，账号锁定 15 分钟
- 支持主动登出

#### 3.1.3 修改密码 / 找回密码

- 已登录用户可在「安全设置」中修改密码，需验证旧密码
- 通过注册邮箱申请重置链接，链接 10 分钟内有效，仅可使用一次

### 3.2 账户管理模块

#### 3.2.1 查询账户信息

用户登录后可查看名下所有账户的基本信息与当前余额。

| 展示字段 |                      说明                       |
| :------: | :---------------------------------------------: |
|  账户号  | 16 位数字，展示时脱敏（如 **** **** **** 1234） |
| 账户类型 |      活期储蓄 / 定期存款（本期仅实现活期）      |
| 账户状态 |               正常 / 冻结 / 注销                |
| 可用余额 |      精确到小数点后 2 位，单位：元（CNY）       |

#### 3.2.2 管理员操作账户

- 管理员可查询任意账户详情
- 管理员可冻结 / 解冻账户（需填写原因，记录操作日志）
- 管理员可注销账户（余额为 0 且无挂起交易时方可操作）

### 3.3 交易服务模块

#### 3.3.1 转账

用户向指定账户号发起转账请求，系统校验后执行原子性资金划拨。

|  步骤  |                             说明                             |
| :----: | :----------------------------------------------------------: |
| ① 输入 |           收款账户号、金额、备注（可选）、支付密码           |
| ② 校验 | 账户存在且正常、余额充足、单笔不超过 50,000 元、日累计不超过 200,000 元 |
| ③ 执行 | `@Transactional` 内同时扣减付款方与增加收款方余额，生成两条交易记录 |
| ④ 通知 |         通过 WebSocket 或邮件推送交易成功 / 失败通知         |

#### 3.3.2 存款

- 仅限管理员或柜台角色操作（模拟柜台存款场景）
- 选择目标账户，输入存款金额，单笔上限 1,000,000 元
- 生成存款交易记录，更新账户余额

#### 3.3.3 取款

- 用户选择账户，输入取款金额与支付密码
- 单笔取款上限 20,000 元，日累计上限 100,000 元
- 余额不足或超限时提示具体原因

### 3.4 账单与报表模块

#### 3.4.1 交易记录查询

用户查询本人账户的历史交易记录，支持多维度筛选。

- 筛选条件：日期范围、交易类型（转入 / 转出 / 存款 / 取款）、金额区间
- 结果按交易时间倒序排列，支持分页（每页 20 条）
- 每条记录展示：交易流水号、类型、金额、对手方账户号（脱敏）、备注、时间

#### 3.4.2 导出对账单

- 支持导出所选时间段内的交易记录为 CSV 或 PDF 格式
- 文件命名格式：账户号_起始日期_结束日期.csv

### 3.5 安全验证模块

- 支付密码：独立于登录密码，用于转账 / 取款等高风险操作，可在安全设置中修改
- OTP 验证码：高额转账（单笔 ≥ 10,000 元）时发送 6 位短信 / 邮件验证码，有效期 5 分钟
- 操作日志：所有账户变更与交易操作记录至 operation_log 表，含操作人、IP、时间

### 3.6 管理员后台模块

#### 3.6.1 用户管理

- 查询所有注册用户列表，支持按姓名、手机号、账户号搜索
- 查看用户详情与名下所有账户
- 启用 / 禁用用户账号（禁用后无法登录）

#### 3.6.2 交易监控

- 查看全平台实时交易流水，支持按时间、账户、金额筛选
- 标记异常交易（可手动标记，触发后台风控提醒）

#### 3.6.3 系统配置

- 配置单笔 / 日累计转账限额
- 配置 OTP 触发阈值
- 查看系统运行状态与错误日志

## 4. 用例描述（关键用例）

### UC-01：用户登录

| 用例元素 |                             内容                             |
| :------: | :----------------------------------------------------------: |
| 用例编号 |                            UC-01                             |
| 用例名称 |                           用户登录                           |
|  参与者  |                           普通用户                           |
| 前置条件 |                        用户已完成注册                        |
|  主流程  | 1. 用户输入用户名和密码 2. 系统验证凭据有效性 3. 生成 JWT Token 并返回 4. 前端存储 Token，跳转至首页 |
| 异常流程 | A1：密码错误 — 返回错误提示，记录失败次数 A2：账号被锁定 — 提示锁定剩余时间 A3：账号被禁用 — 提示联系客服 |
| 后置条件 |              用户获得有效 Token，可访问授权资源              |

### UC-02：账户间转账

| 用例元素 |                             内容                             |
| :------: | :----------------------------------------------------------: |
| 用例编号 |                            UC-02                             |
| 用例名称 |                          账户间转账                          |
|  参与者  |                           普通用户                           |
| 前置条件 |            用户已登录；付款账户状态正常；余额充足            |
|  主流程  | 1. 输入收款账户号、金额、备注 2. 验证收款账户是否存在 3. 输入支付密码 4. 系统执行事务性转账 5. 返回成功通知与交易流水号 |
| 异常流程 | A1：余额不足 — 提示可用余额 A2：超出单笔 / 日限额 — 提示限额 A3：金额 ≥ 10,000 元 — 触发 OTP 验证 A4：支付密码错误 — 最多重试 3 次 |
| 后置条件 |             双方余额更新，生成交易记录，发送通知             |

## 5. 非功能需求

### 5.1 性能需求

- 系统应支持至少 100 个并发用户同时在线
- 普通查询接口响应时间 ≤ 500ms（P95）
- 转账等写操作接口响应时间 ≤ 1000ms（P95）

### 5.2 安全需求

- 所有接口强制 HTTPS 传输（开发阶段可使用 HTTP，部署时开启）
- 密码字段存储 BCrypt 哈希，禁止明文记录
- SQL 语句使用 MyBatis/JPA 参数化查询，防止 SQL 注入
- 关键接口启用 CSRF 防护（Spring Security 默认集成）
- 接口权限遵循最小权限原则，RBAC 控制到方法级别（`@PreAuthorize`）

### 5.3 可靠性需求

- 所有数据库写操作使用 `@Transactional` 保障原子性
- 系统重要操作记录日志，使用 Logback 输出到文件，保留 30 天
- 发生异常时返回规范的错误码与错误信息，不暴露堆栈信息给客户端

### 5.4 可维护性需求

- 代码遵循 Alibaba Java 编码规范，核心业务类注释覆盖率 ≥ 60%
- 遵循三层架构（Controller → Service → Repository），禁止跨层调用
- 异常统一由 GlobalExceptionHandler 处理，不在 Controller 层捕获

### 5.5 可用性需求

- 页面关键操作区域有明确的加载状态与错误提示
- 金额输入限制为正数，最多 2 位小数，前端实时校验
- 重要操作（转账、注销账户）需二次确认弹窗

## 6. 数据需求

### 6.1 核心数据库表设计

#### 6.1.1 主要数据表一览

|      表名      |   中文名   |                           主要字段                           |
| :------------: | :--------: | :----------------------------------------------------------: |
|     users      |   用户表   | id, username, password_hash, full_name, id_card, phone, email, role, status, created_at |
|    accounts    |   账户表   |  id, user_id, account_no, type, balance, status, created_at  |
|  transactions  | 交易流水表 | id, txn_no, from_account, to_account, amount, type, remark, status, created_at |
| operation_logs | 操作日志表 | id, operator_id, operation, target_id, ip_address, detail, created_at |
| system_config  | 系统配置表 |      config_key, config_value, description, updated_at       |

### 6.2 数据字典（关键字段）

|       字段名       |   数据类型    |     约束     |              说明               |
| :----------------: | :-----------: | :----------: | :-----------------------------: |
|      balance       | DECIMAL(15,2) | NOT NULL ≥ 0 |       账户余额，精确到分        |
|       amount       | DECIMAL(15,2) | NOT NULL > 0 |      交易金额，必须为正数       |
|  status (account)  |     ENUM      |   NOT NULL   |    ACTIVE / FROZEN / CLOSED     |
| type (transaction) |     ENUM      |   NOT NULL   | TRANSFER / DEPOSIT / WITHDRAWAL |
|    role (user)     |     ENUM      |   NOT NULL   |          USER / ADMIN           |

## 7. 接口需求（RESTful API 概要）

| 模块 |              路径               |  方法  |            说明            |
| :--: | :-----------------------------: | :----: | :------------------------: |
| 认证 |     POST /api/auth/register     |  公开  |          用户注册          |
| 认证 |      POST /api/auth/login       |  公开  |     用户登录，返回 JWT     |
| 认证 |      POST /api/auth/logout      |  用户  | 主动登出，Token 加入黑名单 |
| 账户 |        GET /api/accounts        |  用户  |    查询当前用户名下账户    |
| 账户 |     GET /api/accounts/{id}      |  用户  |      查询指定账户详情      |
| 交易 | POST /api/transactions/transfer |  用户  |          发起转账          |
| 交易 | POST /api/transactions/deposit  | 管理员 |          柜台存款          |
| 交易 | POST /api/transactions/withdraw |  用户  |            取款            |
| 账单 |  GET /api/transactions/history  |  用户  |      分页查询交易历史      |
| 管理 |      GET /api/admin/users       | 管理员 |        查询所有用户        |
| 管理 | PATCH /api/admin/accounts/{id}  | 管理员 |   冻结 / 解冻 / 注销账户   |

## 8. 项目约束与假设

### 8.1 项目约束

- 开发周期：约 2 周完成
- 团队规模：10 人，按模块分工
- 技术栈固定：后端 Spring Boot，前端不限（推荐 Vue.js 或 Thymeleaf）
- 货币仅支持人民币（CNY），不涉及多币种与外汇兑换
- 暂不实现利息计算、贷款、理财等复杂金融产品

### 8.2 假设与依赖

- 假设短信服务可用 Mock 数据替代（如直接将 OTP 返回到接口响应中）
- 假设测试环境数据库与生产数据库物理隔离
- 前端与后端通过接口文档（Swagger/OpenAPI）约定交互格式

## 9. 附录

### 9.1 推荐项目结构

```
com.bank
│
├── controller
│   ├── UserController
│   ├── AccountController
│   ├── TransferController
│   └── AdminController
│
├── service
│   ├── UserService
│   ├── AccountService
│   ├── TransferService
│   └── TransactionService
│
├── service.impl
│   ├── UserServiceImpl
│   ├── AccountServiceImpl
│   ├── TransferServiceImpl
│   └── TransactionServiceImpl
│
├── repository
│   ├── UserRepository
│   ├── AccountRepository
│   ├── TransferRepository
│   └── TransactionRepository
│
├── model
│   ├── entity
│   │   ├── User
│   │   ├── Account
│   │   ├── Transfer
│   │   └── Transaction
│   │
│   └── dto
│       ├── LoginDTO
│       ├── RegisterDTO
│       ├── TransferDTO
│       └── DepositDTO
│
├── security
│   ├── JwtAuthenticationFilter
│   ├── JwtTokenProvider
│   └── SecurityConfig
│
├── config
│   ├── SwaggerConfig
│   ├── CorsConfig
│   └── WebMvcConfig
│
├── exception
│   ├── BusinessException
│   ├── AccountException
│   └── GlobalExceptionHandler
│
├── util
│   ├── JwtUtil
│   ├── PasswordUtil
│   ├── ResultUtil
│   └── DateUtil
│
└── BankApplication
```

### 9.2 建议分工方案

| 角色建议 |  负责模块   |                  主要任务                   |
| :------: | :---------: | :-----------------------------------------: |
|  成员 A  | 认证 & 安全 | 注册 / 登录、JWT、Spring Security 配置、OTP |
|  成员 B  |  账户管理   |      账户 CRUD、余额查询、账户状态管理      |
|  成员 C  |  交易服务   |         转账、存款、取款、事务控制          |
|  成员 D  | 账单 & 报表 |        交易历史查询、分页、导出功能         |
|  成员 E  | 管理员后台  |        用户管理、交易监控、系统配置         |
|  成员 F  | 前端 & 集成 |   前端页面、API 对接、接口文档（Swagger）   |

### 9.3 文档修订历史

| 版本 |    日期    |    修改人    |          变更说明          |
| :--: | :--------: | :----------: | :------------------------: |
| v1.0 | 2026-06-09 | 小组全体成员 | 初稿完成，覆盖核心功能需求 |
=======
# 网上银行系统

**Software Requirements Specification**

技术栈：Spring Boot・Spring Security・MySQL・Redis・JWT

| 文件版本 |       v1.0       |
| :------: | :--------------: |
| 文件状态 |       草稿       |
| 编写日期 |   2026 年 6 月   |
| 适用范围 | 学生小组课程项目 |

## 1. 引言

### 1.1 编写目的

本文档是网上银行系统（Online Banking System）的软件需求分析说明书，旨在明确系统的功能需求、非功能需求、业务规则及约束条件，为小组成员的后续设计、开发与测试工作提供统一依据。

### 1.2 项目背景

本项目为课程小组实践作业，以模拟真实银行业务场景为目标，基于 Spring Boot 框架构建一套具备基本金融功能的网上银行平台。系统面向普通个人用户和银行管理员两类角色，提供账户管理、资金转账、账单查询等核心金融服务。

### 1.3 术语与缩写

| 术语 / 缩写 |                          说明                           |
| :---------: | :-----------------------------------------------------: |
|     SRS     | Software Requirements Specification，软件需求规格说明书 |
|     JWT     |       JSON Web Token，用于用户身份认证的令牌标准        |
|     API     |   Application Programming Interface，应用程序编程接口   |
|     OTP     |     One-Time Password，一次性验证码（用于安全验证）     |
|    RBAC     |      Role-Based Access Control，基于角色的访问控制      |
|     UC      |                     Use Case，用例                      |

### 1.4 参考资料

- Spring Boot 官方文档：[https://spring.io/projects/spring-boot](https://link.wtturl.cn/?target=https%3A%2F%2Fspring.io%2Fprojects%2Fspring-boot&scene=im&aid=497858&lang=zh)
- Spring Security 官方文档：[https://spring.io/projects/spring-security](https://link.wtturl.cn/?target=https%3A%2F%2Fspring.io%2Fprojects%2Fspring-security&scene=im&aid=497858&lang=zh)
- MySQL 8.0 参考手册
- IEEE 830-1998 软件需求规格说明书标准

## 2. 总体描述

### 2.1 产品概述

网上银行系统是一个基于 B/S 架构的 Web 应用程序，后端采用 Spring Boot + Spring Security，前端可使用 Thymeleaf 或 Vue.js，数据库使用 MySQL，缓存使用 Redis。系统为用户提供全天候的在线金融服务，并为管理员提供后台管理能力。

### 2.2 产品功能概览

系统主要功能模块如下：

- 用户认证与授权模块（注册、登录、JWT 鉴权）
- 账户管理模块（开户、查询、冻结 / 解冻）
- 交易服务模块（转账、存款、取款）
- 账单与报表模块（交易记录查询、导出）
- 安全验证模块（OTP 验证码、操作限额）
- 管理员后台模块（用户管理、交易监控、系统配置）

### 2.3 用户类型与特征

|    角色    |     类型     |                      特征描述                      |
| :--------: | :----------: | :------------------------------------------------: |
|  普通用户  |  已注册客户  | 使用网上银行进行日常金融操作，具备基本网络使用能力 |
| 银行管理员 | 内部工作人员 |   负责账户审核、风控管理、系统配置等后台运营工作   |

### 2.4 运行环境

- 后端：JDK 17+，Spring Boot 3.x，Maven 3.8+
- 数据库：MySQL 8.0+，Redis 6.x+
- 服务器：本地开发环境 / Linux 云服务器（Tomcat 内嵌）
- 客户端：现代浏览器（Chrome、Firefox、Edge）

### 2.5 设计与实现约束

- 使用 Spring Boot 作为核心框架，不得替换为其他框架
- 所有 API 接口遵循 RESTful 设计风格，统一返回 JSON 格式数据
- 敏感数据（密码、身份证号）必须加密存储，密码使用 BCrypt 哈希
- 所有金融交易操作必须在数据库事务（`@Transactional`）内完成
- 接口权限控制基于 Spring Security + JWT 实现

## 3. 功能需求

### 3.1 用户认证模块

#### 3.1.1 用户注册

用户提交个人信息完成账号注册，系统自动创建关联银行账户。

|   字段   |                             说明                             |
| :------: | :----------------------------------------------------------: |
|   输入   |     用户名、密码、确认密码、姓名、身份证号、手机号、邮箱     |
|   处理   | 校验字段格式与唯一性；BCrypt 加密密码；生成 16 位账户号；创建账户记录（初始余额 0.00） |
|   输出   |     注册成功提示及分配的账户号；或字段校验失败的错误信息     |
| 业务规则 |    用户名与手机号全局唯一；密码至少 8 位且包含字母与数字     |

#### 3.1.2 用户登录

用户使用用户名与密码登录系统，服务端签发 JWT Token。

- 成功登录返回 accessToken（有效期 30 分钟）与 refreshToken（有效期 7 天）
- 连续登录失败 5 次，账号锁定 15 分钟
- 支持主动登出

#### 3.1.3 修改密码 / 找回密码

- 已登录用户可在「安全设置」中修改密码，需验证旧密码
- 通过注册邮箱申请重置链接，链接 10 分钟内有效，仅可使用一次

### 3.2 账户管理模块

#### 3.2.1 查询账户信息

用户登录后可查看名下所有账户的基本信息与当前余额。

| 展示字段 |                      说明                       |
| :------: | :---------------------------------------------: |
|  账户号  | 16 位数字，展示时脱敏（如 **** **** **** 1234） |
| 账户类型 |      活期储蓄 / 定期存款（本期仅实现活期）      |
| 账户状态 |               正常 / 冻结 / 注销                |
| 可用余额 |      精确到小数点后 2 位，单位：元（CNY）       |

#### 3.2.2 管理员操作账户

- 管理员可查询任意账户详情
- 管理员可冻结 / 解冻账户（需填写原因，记录操作日志）
- 管理员可注销账户（余额为 0 且无挂起交易时方可操作）

### 3.3 交易服务模块

#### 3.3.1 转账

用户向指定账户号发起转账请求，系统校验后执行原子性资金划拨。

|  步骤  |                             说明                             |
| :----: | :----------------------------------------------------------: |
| ① 输入 |           收款账户号、金额、备注（可选）、支付密码           |
| ② 校验 | 账户存在且正常、余额充足、单笔不超过 50,000 元、日累计不超过 200,000 元 |
| ③ 执行 | `@Transactional` 内同时扣减付款方与增加收款方余额，生成两条交易记录 |
| ④ 通知 |         通过 WebSocket 或邮件推送交易成功 / 失败通知         |

#### 3.3.2 存款

- 仅限管理员或柜台角色操作（模拟柜台存款场景）
- 选择目标账户，输入存款金额，单笔上限 1,000,000 元
- 生成存款交易记录，更新账户余额

#### 3.3.3 取款

- 用户选择账户，输入取款金额与支付密码
- 单笔取款上限 20,000 元，日累计上限 100,000 元
- 余额不足或超限时提示具体原因

### 3.4 账单与报表模块

#### 3.4.1 交易记录查询

用户查询本人账户的历史交易记录，支持多维度筛选。

- 筛选条件：日期范围、交易类型（转入 / 转出 / 存款 / 取款）、金额区间
- 结果按交易时间倒序排列，支持分页（每页 20 条）
- 每条记录展示：交易流水号、类型、金额、对手方账户号（脱敏）、备注、时间

#### 3.4.2 导出对账单

- 支持导出所选时间段内的交易记录为 CSV 或 PDF 格式
- 文件命名格式：账户号_起始日期_结束日期.csv

### 3.5 安全验证模块

- 支付密码：独立于登录密码，用于转账 / 取款等高风险操作，可在安全设置中修改
- OTP 验证码：高额转账（单笔 ≥ 10,000 元）时发送 6 位短信 / 邮件验证码，有效期 5 分钟
- 操作日志：所有账户变更与交易操作记录至 operation_log 表，含操作人、IP、时间

### 3.6 管理员后台模块

#### 3.6.1 用户管理

- 查询所有注册用户列表，支持按姓名、手机号、账户号搜索
- 查看用户详情与名下所有账户
- 启用 / 禁用用户账号（禁用后无法登录）

#### 3.6.2 交易监控

- 查看全平台实时交易流水，支持按时间、账户、金额筛选
- 标记异常交易（可手动标记，触发后台风控提醒）

#### 3.6.3 系统配置

- 配置单笔 / 日累计转账限额
- 配置 OTP 触发阈值
- 查看系统运行状态与错误日志

## 4. 用例描述（关键用例）

### UC-01：用户登录

| 用例元素 |                             内容                             |
| :------: | :----------------------------------------------------------: |
| 用例编号 |                            UC-01                             |
| 用例名称 |                           用户登录                           |
|  参与者  |                           普通用户                           |
| 前置条件 |                        用户已完成注册                        |
|  主流程  | 1. 用户输入用户名和密码 2. 系统验证凭据有效性 3. 生成 JWT Token 并返回 4. 前端存储 Token，跳转至首页 |
| 异常流程 | A1：密码错误 — 返回错误提示，记录失败次数 A2：账号被锁定 — 提示锁定剩余时间 A3：账号被禁用 — 提示联系客服 |
| 后置条件 |              用户获得有效 Token，可访问授权资源              |

### UC-02：账户间转账

| 用例元素 |                             内容                             |
| :------: | :----------------------------------------------------------: |
| 用例编号 |                            UC-02                             |
| 用例名称 |                          账户间转账                          |
|  参与者  |                           普通用户                           |
| 前置条件 |            用户已登录；付款账户状态正常；余额充足            |
|  主流程  | 1. 输入收款账户号、金额、备注 2. 验证收款账户是否存在 3. 输入支付密码 4. 系统执行事务性转账 5. 返回成功通知与交易流水号 |
| 异常流程 | A1：余额不足 — 提示可用余额 A2：超出单笔 / 日限额 — 提示限额 A3：金额 ≥ 10,000 元 — 触发 OTP 验证 A4：支付密码错误 — 最多重试 3 次 |
| 后置条件 |             双方余额更新，生成交易记录，发送通知             |

## 5. 非功能需求

### 5.1 性能需求

- 系统应支持至少 100 个并发用户同时在线
- 普通查询接口响应时间 ≤ 500ms（P95）
- 转账等写操作接口响应时间 ≤ 1000ms（P95）

### 5.2 安全需求

- 所有接口强制 HTTPS 传输（开发阶段可使用 HTTP，部署时开启）
- 密码字段存储 BCrypt 哈希，禁止明文记录
- SQL 语句使用 MyBatis/JPA 参数化查询，防止 SQL 注入
- 关键接口启用 CSRF 防护（Spring Security 默认集成）
- 接口权限遵循最小权限原则，RBAC 控制到方法级别（`@PreAuthorize`）

### 5.3 可靠性需求

- 所有数据库写操作使用 `@Transactional` 保障原子性
- 系统重要操作记录日志，使用 Logback 输出到文件，保留 30 天
- 发生异常时返回规范的错误码与错误信息，不暴露堆栈信息给客户端

### 5.4 可维护性需求

- 代码遵循 Alibaba Java 编码规范，核心业务类注释覆盖率 ≥ 60%
- 遵循三层架构（Controller → Service → Repository），禁止跨层调用
- 异常统一由 GlobalExceptionHandler 处理，不在 Controller 层捕获

### 5.5 可用性需求

- 页面关键操作区域有明确的加载状态与错误提示
- 金额输入限制为正数，最多 2 位小数，前端实时校验
- 重要操作（转账、注销账户）需二次确认弹窗

## 6. 数据需求

### 6.1 核心数据库表设计

#### 6.1.1 主要数据表一览

|      表名      |   中文名   |                           主要字段                           |
| :------------: | :--------: | :----------------------------------------------------------: |
|     users      |   用户表   | id, username, password_hash, full_name, id_card, phone, email, role, status, created_at |
|    accounts    |   账户表   |  id, user_id, account_no, type, balance, status, created_at  |
|  transactions  | 交易流水表 | id, txn_no, from_account, to_account, amount, type, remark, status, created_at |
| operation_logs | 操作日志表 | id, operator_id, operation, target_id, ip_address, detail, created_at |
| system_config  | 系统配置表 |      config_key, config_value, description, updated_at       |

### 6.2 数据字典（关键字段）

|       字段名       |   数据类型    |     约束     |              说明               |
| :----------------: | :-----------: | :----------: | :-----------------------------: |
|      balance       | DECIMAL(15,2) | NOT NULL ≥ 0 |       账户余额，精确到分        |
|       amount       | DECIMAL(15,2) | NOT NULL > 0 |      交易金额，必须为正数       |
|  status (account)  |     ENUM      |   NOT NULL   |    ACTIVE / FROZEN / CLOSED     |
| type (transaction) |     ENUM      |   NOT NULL   | TRANSFER / DEPOSIT / WITHDRAWAL |
|    role (user)     |     ENUM      |   NOT NULL   |          USER / ADMIN           |

## 7. 接口需求（RESTful API 概要）

| 模块 |              路径               |  方法  |            说明            |
| :--: | :-----------------------------: | :----: | :------------------------: |
| 认证 |     POST /api/auth/register     |  公开  |          用户注册          |
| 认证 |      POST /api/auth/login       |  公开  |     用户登录，返回 JWT     |
| 认证 |      POST /api/auth/logout      |  用户  | 主动登出，Token 加入黑名单 |
| 账户 |        GET /api/accounts        |  用户  |    查询当前用户名下账户    |
| 账户 |     GET /api/accounts/{id}      |  用户  |      查询指定账户详情      |
| 交易 | POST /api/transactions/transfer |  用户  |          发起转账          |
| 交易 | POST /api/transactions/deposit  | 管理员 |          柜台存款          |
| 交易 | POST /api/transactions/withdraw |  用户  |            取款            |
| 账单 |  GET /api/transactions/history  |  用户  |      分页查询交易历史      |
| 管理 |      GET /api/admin/users       | 管理员 |        查询所有用户        |
| 管理 | PATCH /api/admin/accounts/{id}  | 管理员 |   冻结 / 解冻 / 注销账户   |

## 8. 项目约束与假设

### 8.1 项目约束

- 开发周期：约 2 周完成
- 团队规模：10 人，按模块分工
- 技术栈固定：后端 Spring Boot，前端不限（推荐 Vue.js 或 Thymeleaf）
- 货币仅支持人民币（CNY），不涉及多币种与外汇兑换
- 暂不实现利息计算、贷款、理财等复杂金融产品

### 8.2 假设与依赖

- 假设短信服务可用 Mock 数据替代（如直接将 OTP 返回到接口响应中）
- 假设测试环境数据库与生产数据库物理隔离
- 前端与后端通过接口文档（Swagger/OpenAPI）约定交互格式

## 9. 附录

### 9.1 推荐项目结构

```
com.bank
│
├── controller
│   ├── UserController
│   ├── AccountController
│   ├── TransferController
│   └── AdminController
│
├── service
│   ├── UserService
│   ├── AccountService
│   ├── TransferService
│   └── TransactionService
│
├── service.impl
│   ├── UserServiceImpl
│   ├── AccountServiceImpl
│   ├── TransferServiceImpl
│   └── TransactionServiceImpl
│
├── repository
│   ├── UserRepository
│   ├── AccountRepository
│   ├── TransferRepository
│   └── TransactionRepository
│
├── model
│   ├── entity
│   │   ├── User
│   │   ├── Account
│   │   ├── Transfer
│   │   └── Transaction
│   │
│   └── dto
│       ├── LoginDTO
│       ├── RegisterDTO
│       ├── TransferDTO
│       └── DepositDTO
│
├── security
│   ├── JwtAuthenticationFilter
│   ├── JwtTokenProvider
│   └── SecurityConfig
│
├── config
│   ├── SwaggerConfig
│   ├── CorsConfig
│   └── WebMvcConfig
│
├── exception
│   ├── BusinessException
│   ├── AccountException
│   └── GlobalExceptionHandler
│
├── util
│   ├── JwtUtil
│   ├── PasswordUtil
│   ├── ResultUtil
│   └── DateUtil
│
└── BankApplication
```

### 9.2 建议分工方案

| 角色建议 |  负责模块   |                  主要任务                   |
| :------: | :---------: | :-----------------------------------------: |
|  成员 A  | 认证 & 安全 | 注册 / 登录、JWT、Spring Security 配置、OTP |
|  成员 B  |  账户管理   |      账户 CRUD、余额查询、账户状态管理      |
|  成员 C  |  交易服务   |         转账、存款、取款、事务控制          |
|  成员 D  | 账单 & 报表 |        交易历史查询、分页、导出功能         |
|  成员 E  | 管理员后台  |        用户管理、交易监控、系统配置         |
|  成员 F  | 前端 & 集成 |   前端页面、API 对接、接口文档（Swagger）   |

### 9.3 文档修订历史

| 版本 |    日期    |    修改人    |          变更说明          |
| :--: | :--------: | :----------: | :------------------------: |
| v1.0 | 2026-06-09 | 小组全体成员 | 初稿完成，覆盖核心功能需求 |
>>>>>>> 50afff089401006726a531dc2e3c3ff201887418
