@echo off
chcp 65001 >nul
cd /d "%~dp0.."

echo [1/2] 启动 MySQL...
docker compose up -d
timeout /t 10 /nobreak >nul

echo [2/2] 启动网上银行系统...
mvn spring-boot:run
