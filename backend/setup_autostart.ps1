# PowerShell скрипт для настройки автозапуска сервера
# Запустите этот скрипт от имени администратора

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Настройка автозапуска BTEU Schedule Server" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Проверка прав администратора
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "⚠️  ВНИМАНИЕ: Скрипт должен быть запущен от имени администратора!" -ForegroundColor Yellow
    Write-Host "   Нажмите правой кнопкой мыши и выберите 'Запуск от имени администратора'" -ForegroundColor Yellow
    Write-Host ""
    $response = Read-Host "Продолжить? (y/n)"
    if ($response -ne "y") {
        exit
    }
}

# Получаем путь к текущей директории
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendPath = $scriptPath
$serverScript = Join-Path $backendPath "server.py"
$pythonPath = (Get-Command python -ErrorAction SilentlyContinue).Source

if (-not $pythonPath) {
    Write-Host "❌ Python не найден в PATH!" -ForegroundColor Red
    Write-Host "   Установите Python и добавьте его в PATH" -ForegroundColor Red
    Write-Host "   Или укажите полный путь к python.exe" -ForegroundColor Yellow
    $pythonPath = Read-Host "Введите полный путь к python.exe (или нажмите Enter для выхода)"
    if ([string]::IsNullOrWhiteSpace($pythonPath)) {
        exit
    }
}

if (-not (Test-Path $serverScript)) {
    Write-Host "❌ Файл server.py не найден в $backendPath" -ForegroundColor Red
    exit
}

Write-Host "✅ Python найден: $pythonPath" -ForegroundColor Green
Write-Host "✅ Сервер найден: $serverScript" -ForegroundColor Green

# Проверка .env файла
$envFile = Join-Path $backendPath ".env"
if (-not (Test-Path $envFile)) {
    Write-Host "⚠️  Файл .env не найден!" -ForegroundColor Yellow
    Write-Host "   Сервер может не работать без настроек БД" -ForegroundColor Yellow
} else {
    Write-Host "✅ Файл .env найден" -ForegroundColor Green
}

Write-Host ""

# Создаем bat-файл для запуска сервера в фоне
$startupBat = Join-Path $backendPath "start_server_hidden.bat"
$logFile = Join-Path $backendPath "server.log"

# Нормализуем пути (убираем кавычки, если есть)
$pythonPathNormalized = $pythonPath.Trim('"')
$backendPathNormalized = $backendPath.Trim('"')
$serverScriptNormalized = $serverScript.Trim('"')

$batContent = @"
@echo off
chcp 65001 >nul
cd /d "$backendPathNormalized"

REM Добавляем текущую дату и время в лог
echo ======================================== >> "$logFile"
echo [%date% %time%] Запуск сервера >> "$logFile"
echo ======================================== >> "$logFile"

REM Проверяем наличие server.py
if not exist "server.py" (
    echo [%date% %time%] [ERROR] Файл server.py не найден! >> "$logFile"
    exit /b 1
)

REM Запускаем сервер и перенаправляем вывод в лог
"$pythonPathNormalized" "$serverScriptNormalized" >> "$logFile" 2>&1

REM Если сервер завершился с ошибкой, записываем в лог
if errorlevel 1 (
    echo [%date% %time%] [ERROR] Сервер завершился с ошибкой! >> "$logFile"
)
"@

$batContent | Out-File -FilePath $startupBat -Encoding UTF8
Write-Host "✅ Создан скрипт запуска: $startupBat" -ForegroundColor Green

# Создаем задачу в планировщике заданий
$taskName = "BTEU_Schedule_Server"
$taskDescription = "Автоматический запуск BTEU Schedule Backend Server при входе в систему"

Write-Host ""
Write-Host "Создание задачи в планировщике заданий..." -ForegroundColor Yellow

# Удаляем существующую задачу, если есть
$existingTask = Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
if ($existingTask) {
    Write-Host "⚠️  Задача уже существует. Удаляем старую..." -ForegroundColor Yellow
    Unregister-ScheduledTask -TaskName $taskName -Confirm:$false -ErrorAction SilentlyContinue
}

# Создаем действие (запуск bat-файла)
$action = New-ScheduledTaskAction -Execute $startupBat -WorkingDirectory $backendPath

# Создаем триггер (при входе в систему с задержкой 30 секунд)
# Задержка нужна, чтобы дать время PostgreSQL и другим службам запуститься
$trigger = New-ScheduledTaskTrigger -AtLogOn
$trigger.Delay = "PT30S"  # Задержка 30 секунд

# Настройки задачи
# - AllowStartIfOnBatteries: разрешить запуск на батарее
# - DontStopIfGoingOnBatteries: не останавливать при переходе на батарею
# - StartWhenAvailable: запускать, когда доступно
# - RestartCount: количество попыток перезапуска при сбое
# - RestartInterval: интервал между попытками перезапуска
$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -StartWhenAvailable `
    -RunOnlyIfNetworkAvailable:$false `
    -RestartCount 3 `
    -RestartInterval (New-TimeSpan -Minutes 1)

# Создаем задачу
try {
    Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger -Settings $settings -Description $taskDescription -RunLevel Highest | Out-Null
    Write-Host "✅ Задача успешно создана в планировщике заданий!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Задача будет запускаться автоматически при входе в систему" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Ошибка при создании задачи: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Альтернативный способ: добавьте ярлык в папку автозагрузки" -ForegroundColor Yellow
    Write-Host "   Папка: $env:APPDATA\Microsoft\Windows\Start Menu\Programs\Startup" -ForegroundColor Yellow
    exit
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Настройка завершена!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Информация:" -ForegroundColor Yellow
Write-Host "  • Задача: $taskName" -ForegroundColor White
Write-Host "  • Логи сервера: $backendPath\server.log" -ForegroundColor White
Write-Host "  • Управление: Планировщик заданий → $taskName" -ForegroundColor White
Write-Host ""
Write-Host "Проверка работы:" -ForegroundColor Yellow
Write-Host "  1. Перезагрузите компьютер" -ForegroundColor White
Write-Host "  2. Откройте: http://localhost:8000/v1/health" -ForegroundColor White
Write-Host "  3. Или проверьте логи: type $backendPath\server.log" -ForegroundColor White
Write-Host ""
Write-Host "Управление задачей:" -ForegroundColor Yellow
Write-Host "  • Запустить сейчас: Start-ScheduledTask -TaskName '$taskName'" -ForegroundColor White
Write-Host "  • Остановить: Stop-ScheduledTask -TaskName '$taskName'" -ForegroundColor White
Write-Host "  • Удалить: Unregister-ScheduledTask -TaskName '$taskName'" -ForegroundColor White
Write-Host ""
Write-Host "Нажмите Enter для выхода..."
Read-Host | Out-Null

