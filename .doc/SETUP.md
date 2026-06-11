# 部署指南

详见仓库根目录 [README.md](../README.md)。

## 目录说明

- `project/front` — 前端页面与静态资源
- `project/back` — 后端 Spring Boot 工程（含 SQL、Docker、脚本）

## IDEA 配置

1. 用 IDEA 打开**仓库根目录**（含根 `pom.xml`）
2. Maven 面板 → **Reload All Maven Projects**
3. 运行预置配置 **OnlineBankApplication**
   - Main class: `com.banking.OnlineBankApplication`
   - Working directory: `project/back`
4. 若报 `ClassNotFoundException`：Build → Rebuild Project，或删除根目录残留的 `target/` 文件夹

## 常见问题

**启动后进程立即退出（MySQL 连接失败）**

1. 确认 Docker 已启动：`docker compose up -d`（在 `project/back` 下）
2. 确认 `application.yml` 密码为 `root123`（与 docker-compose 一致）
3. 等待 MySQL 健康检查通过后再启动应用
