# PowerShell скрипт для автоматической настройки автозапуска сервера
# Пытается использовать планировщик заданий, если нет прав - использует папку автозагрузки

$ErrorActionPreference = "Stop"

# Получаем путь к текущей директории
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendPath = $scriptPath
$serverScript = Join-Path $backendPath "server.py"
$pythonPath = (Get-Command python -ErrorAction SilentlyContinue).Source

if (-not $pythonPath) {
    Write-Host "Python не найден в PATH!" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $serverScript)) {
    Write-Host "Файл server.py не найден!" -ForegroundColor Red
    exit 1
}

Write-Host "Python найден: $pythonPath" -ForegroundColor Green
Write-Host "Сервер найден: $serverScript" -ForegroundColor Green

# Создаем bat-файл для запуска сервера
$startupBat = Join-Path $backendPath "start_server_hidden.bat"
$logFile = Join-Path $backendPath "server.log"

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
Write-Host "Создан скрипт запуска: $startupBat" -ForegroundColor Green

# Проверяем права администратора
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if ($isAdmin) {
    # Пытаемся создать задачу в планировщике заданий
    $taskName = "BTEU_Schedule_Server"
    $taskDescription = "Автоматический запуск BTEU Schedule Backend Server при входе в систему"
    
    Write-Host "Попытка создать задачу в планировщике заданий..." -ForegroundColor Yellow
    
    # Удаляем существующую задачу, если есть
    $existingTask = Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
    if ($existingTask) {
        Write-Host "Задача уже существует. Удаляем старую..." -ForegroundColor Yellow
        Unregister-ScheduledTask -TaskName $taskName -Confirm:$false -ErrorAction SilentlyContinue
    }
    
    try {
        # Создаем действие
        $action = New-ScheduledTaskAction -Execute $startupBat -WorkingDirectory $backendPath
        
        # Создаем триггер (при входе в систему с задержкой 30 секунд)
        $trigger = New-ScheduledTaskTrigger -AtLogOn
        $trigger.Delay = "PT30S"
        
        # Настройки задачи
        $settings = New-ScheduledTaskSettingsSet `
            -AllowStartIfOnBatteries `
            -DontStopIfGoingOnBatteries `
            -StartWhenAvailable `
            -RunOnlyIfNetworkAvailable:$false `
            -RestartCount 3 `
            -RestartInterval (New-TimeSpan -Minutes 1)
        
        Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger -Settings $settings -Description $taskDescription -RunLevel Highest | Out-Null
        Write-Host "Задача успешно создана в планировщике заданий!" -ForegroundColor Green
        Write-Host "Задача: $taskName" -ForegroundColor White
    } catch {
        Write-Host "Ошибка при создании задачи в планировщике: $_" -ForegroundColor Yellow
        Write-Host "Используем альтернативный способ (папка автозагрузки)..." -ForegroundColor Yellow
        $isAdmin = $false
    }
}

if (-not $isAdmin) {
    # Используем папку автозагрузки
    $startupFolder = "$env:APPDATA\Microsoft\Windows\Start Menu\Programs\Startup"
    $shortcutPath = Join-Path $startupFolder "BTEU_Schedule_Server.lnk"
    
    Write-Host "Создание ярлыка в папке автозагрузки..." -ForegroundColor Yellow
    
    try {
        $WshShell = New-Object -ComObject WScript.Shell
        $Shortcut = $WshShell.CreateShortcut($shortcutPath)
        $Shortcut.TargetPath = $startupBat
        $Shortcut.WorkingDirectory = $backendPath
        $Shortcut.WindowStyle = 7  # Скрытое окно
        $Shortcut.Description = "BTEU Schedule Server Autostart"
        $Shortcut.Save()
        
        Write-Host "Ярлык успешно создан в папке автозагрузки!" -ForegroundColor Green
        Write-Host "Путь: $shortcutPath" -ForegroundColor White
    } catch {
        Write-Host "Ошибка при создании ярлыка: $_" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "Настройка завершена!" -ForegroundColor Green
Write-Host "Логи сервера: $logFile" -ForegroundColor White
Write-Host ""
Write-Host "Проверка работы:" -ForegroundColor Yellow
Write-Host "  1. Перезагрузите компьютер" -ForegroundColor White
Write-Host "  2. Откройте: http://localhost:8000/v1/health" -ForegroundColor White
Write-Host "  3. Или проверьте логи: type $logFile" -ForegroundColor White

