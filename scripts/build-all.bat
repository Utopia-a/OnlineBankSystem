@echo off
chcp 65001 >nul
cd /d "%~dp0.."
echo 编译单体项目 online-bank-system ...
mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
echo.
echo 构建完成: target\online-bank-system-1.0.0.jar
pause
