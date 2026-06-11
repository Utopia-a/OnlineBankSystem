# 网上银行系统（Online Bank System）

## 项目结构

```
OnlineBankSystem/
├── .doc/                  # 项目文档
├── .git/
├── README.md              # 本文件
└── project/
    ├── front/             # 前端：Thymeleaf 模板 + 静态资源
    │   ├── templates/
    │   └── static/
    └── back/              # 后端：Spring Boot 单体应用
        ├── pom.xml
        ├── src/
        ├── sql/
        ├── docker-compose.yml
        └── scripts/
```

## 快速启动

### 1. 启动 MySQL

在 `project/back` 目录下：

```bash
docker compose up -d
```

### 2. 启动后端（同时加载前端页面）

```bash
cd project/back
mvn spring-boot:run
```

或双击 `project/back/scripts/start.bat`。

**IDEA 运行配置：**

1. 用 IDEA 打开**仓库根目录** `OnlineBankSystem`（根目录已有 `pom.xml` 聚合工程）
2. 右侧 Maven 面板点击 **Reload All Maven Projects**（刷新 Maven 项目）
3. 使用运行配置 **OnlineBankApplication**（已预置）
   - 主类：`com.banking.OnlineBankApplication`
   - 工作目录：`project/back`

若仍报 `ClassNotFoundException`，删除旧的运行配置，重新从 `OnlineBankApplication.java` 右键 Run。

### 3. 访问

| 地址 | 说明 |
|------|------|
| http://localhost:8080/login | 登录页 |
| http://localhost:8080/swagger-ui.html | API 文档 |

## 数据库

- 库名：`banking_db`
- 用户：`root` / `root123`
- 初始化脚本：`project/back/sql/schema.sql`

## 管理员账号

- 用户名：`admin`
- 密码：`Admin@123`

若登录失败，执行 `project/back/sql/fix-admin-password.sql`。
