# Скрипт для проверки и запуска сервера
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
Write-Host "Проверка статуса BTEU Schedule Server" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Проверка, запущен ли сервер
try {
    $response = Invoke-WebRequest -Uri 'http://localhost:8000/v1/health' -TimeoutSec 2 -UseBasicParsing -ErrorAction Stop
    Write-Host "✅ Сервер УЖЕ ЗАПУЩЕН на порту 8000" -ForegroundColor Green
    Write-Host "   Статус: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "   Ответ: $($response.Content)" -ForegroundColor Green
    exit 0
} catch {
    Write-Host "❌ Сервер НЕ запущен" -ForegroundColor Red
    Write-Host ""
}

# Проверка наличия Python
Write-Host "Проверка Python..." -ForegroundColor Yellow
try {
    $pythonVersion = python --version 2>&1
    Write-Host "✅ Python найден: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Python не найден! Установите Python с https://www.python.org/" -ForegroundColor Red
    exit 1
}

# Проверка наличия файла server.py
if (-not (Test-Path "server.py")) {
    Write-Host "❌ Файл server.py не найден в текущей директории!" -ForegroundColor Red
    Write-Host "   Текущая директория: $(Get-Location)" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Файл server.py найден" -ForegroundColor Green
Write-Host ""

# Проверка зависимостей
Write-Host "Проверка зависимостей..." -ForegroundColor Yellow
try {
    python -c "import flask" 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "⚠️ Flask не установлен. Установка зависимостей..." -ForegroundColor Yellow
        pip install -r requirements.txt
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Не удалось установить зависимости!" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "✅ Зависимости установлены" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️ Ошибка проверки зависимостей: $_" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
Write-Host "Запуск сервера..." -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "Сервер будет доступен на:" -ForegroundColor Yellow
Write-Host "  - http://localhost:8000 (с компьютера)" -ForegroundColor White
Write-Host "  - http://10.0.2.2:8000 (из Android эмулятора)" -ForegroundColor White
Write-Host "  - http://192.168.1.25:8000 (из Android устройства в той же сети)" -ForegroundColor White
Write-Host ""
Write-Host "Для остановки нажмите Ctrl+C" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Запуск сервера
python server.py

