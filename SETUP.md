# 网上银行系统 - 部署指南

## 项目结构（单体应用）

```
OnlineBankSystem/
├── pom.xml                          # 唯一 Maven 配置
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/banking/         # 启动类、统一配置
│   │   │   ├── com/banking/auth/    # 认证模块
│   │   │   ├── com/bank/account/    # 账户模块
│   │   │   ├── com/bank/transaction/# 交易模块
│   │   │   ├── com/banking/report/  # 报表模块
│   │   │   └── com/bank/web/        # Web API 控制器
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── templates/           # Thymeleaf 页面
│   │       └── static/              # CSS/JS
│   └── test/java/
├── sql/schema.sql
├── docker-compose.yml
└── scripts/
```

## 环境要求

- JDK 17+
- Maven 3.8+
- Docker Desktop（MySQL）

## 快速启动

### 1. 启动数据库

```bash
docker compose up -d
```

### 2. 编译并运行

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

或双击 `scripts/start.bat`。

### 3. 访问

| 地址 | 说明 |
|------|------|
| http://localhost:8080 | 网上银行首页 |
| http://localhost:8080/swagger-ui.html | API 文档 |

## 功能模块

所有功能运行在 **单个进程、端口 8080**：

- 用户注册/登录（JWT）
- 账户开户/查询
- 转账/存款/取款
- 交易历史查询与导出

## 数据库

- 库名：`banking_db`
- 用户：`root` / `root123`
- 初始化：`sql/schema.sql`（Docker 首次启动自动执行）

## 注册说明

- 密码至少 8 位，含大小写字母和数字
- 注册后需邮箱验证才能登录（OTP 打印在控制台日志）
- 验证接口：`POST /api/auth/verify-email`
