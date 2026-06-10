# 网上银行系统 - 部署指南

## 单体 Maven 项目（唯一 `src`）

本项目已**彻底合并**为单个 Maven 工程，无子模块、无多个 `pom.xml`：

```
OnlineBankSystem/
├── pom.xml                 # 唯一 Maven 配置
├── src/
│   ├── main/
│   │   ├── java/           # 全部后端源码（A–F 各成员模块）
│   │   └── resources/      # 配置、Thymeleaf 页面、静态资源
│   └── test/java/          # 单元测试
├── sql/                    # 数据库脚本
├── docker-compose.yml      # MySQL
└── scripts/                # 启动脚本
```

### 源码包说明（同一 `src` 内按包划分）

| 包路径 | 模块 |
|--------|------|
| `com.banking` | 启动类、统一 Security / 异常 / OpenAPI |
| `com.banking.auth` | 认证（B） |
| `com.bank.account` | 账户（C） |
| `com.bank.transaction` | 交易（D） |
| `com.banking.report` | 报表（E） |
| `com.bank.admin` | 管理员后台（F） |
| `com.bank.web` / `com.bank.controller` | Web API 与页面路由（A） |

## 环境要求

- JDK 17+
- Maven 3.8+
- Docker Desktop（MySQL，可选）

## 快速启动

### 1. 启动数据库

```bash
docker compose up -d
```

### 2. 编译并运行（唯一入口）

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

主类：`com.banking.OnlineBankApplication`  
或双击 `scripts/start.bat`。

### 3. 访问

| 地址 | 说明 |
|------|------|
| http://localhost:8080/login | 登录页 |
| http://localhost:8080/swagger-ui.html | API 文档 |

## 管理员账号

- 用户名：`admin`
- 密码：`Admin@123`

若登录失败，执行 `sql/fix-admin-password.sql`。

## 数据库

- 库名：`banking_db`
- 初始化：`sql/schema.sql`
