@echo off
chcp 65001 >nul
echo ========================================
echo Проверка статуса BTEU Schedule Server
echo ========================================
echo.

REM Проверка процесса Python с server.py
tasklist /FI "IMAGENAME eq python.exe" /FO CSV | findstr /I "server.py" >nul 2>&1
if errorlevel 1 (
    echo ❌ Сервер НЕ запущен
    echo.
    echo Проверьте:
    echo   1. Запущен ли автозапуск (см. планировщик заданий)
    echo   2. Логи: type server.log
) else (
    echo ✅ Сервер ЗАПУЩЕН
    echo.
    echo Процессы Python:
    tasklist /FI "IMAGENAME eq python.exe" | findstr python
)

echo.
echo ========================================
echo Проверка доступности API
echo ========================================
echo.

REM Проверка доступности через curl (если установлен)
curl --version >nul 2>&1
if errorlevel 1 (
    echo ⚠️  curl не установлен, проверка через PowerShell...
    powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8000/v1/health' -TimeoutSec 3 -UseBasicParsing; Write-Host '✅ API доступен:' $response.StatusCode; Write-Host $response.Content } catch { Write-Host '❌ API недоступен:' $_.Exception.Message }"
) else (
    echo Проверка http://localhost:8000/v1/health
    curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://localhost:8000/v1/health
    if errorlevel 1 (
        echo ❌ API недоступен
    ) else (
        echo ✅ API доступен
    )
)

echo.
echo ========================================
echo Последние записи из лога
echo ========================================
echo.

if exist "server.log" (
    echo Последние 10 строк лога:
    echo.
    powershell -Command "Get-Content server.log -Tail 10"
) else (
    echo ⚠️  Файл server.log не найден
    echo    Сервер, возможно, еще не запускался
)

echo.
echo ========================================
echo Нажмите любую клавишу для выхода...
pause >nul

