# Скрипт для проверки и запуска локального сервера
Write-Host "🔍 Проверка локального сервера..." -ForegroundColor Cyan

# Проверка, запущен ли сервер на порту 8000
$portCheck = netstat -an | Select-String ":8000"
if ($portCheck) {
    Write-Host "✅ Сервер уже запущен на порту 8000" -ForegroundColor Green
    Write-Host "   Сервер доступен по адресу: http://localhost:8000" -ForegroundColor Gray
    Write-Host "   Для эмулятора: http://10.0.2.2:8000" -ForegroundColor Gray
    exit 0
}

Write-Host "⚠️  Сервер не запущен" -ForegroundColor Yellow
Write-Host "`nЗапуск сервера..." -ForegroundColor Cyan

# Переход в папку backend
Set-Location -Path "backend"

# Проверка наличия Python
try {
    $pythonVersion = python --version 2>&1
    Write-Host "✅ Python найден: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Python не найден! Установите Python с https://www.python.org/" -ForegroundColor Red
    exit 1
}

# Проверка наличия server.py
if (-not (Test-Path "server.py")) {
    Write-Host "❌ Файл server.py не найден в папке backend!" -ForegroundColor Red
    exit 1
}

Write-Host "`n🚀 Запуск сервера..." -ForegroundColor Cyan
Write-Host "   Сервер будет доступен на:" -ForegroundColor Gray
Write-Host "   - http://localhost:8000 (с компьютера)" -ForegroundColor Gray
Write-Host "   - http://10.0.2.2:8000 (из Android эмулятора)" -ForegroundColor Gray
Write-Host "`n   Для остановки нажмите Ctrl+C" -ForegroundColor Yellow
Write-Host "`n" -ForegroundColor White

# Запуск сервера
python server.py

