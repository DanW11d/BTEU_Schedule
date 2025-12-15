# Скрипт для очистки lock-файлов эмулятора Android
# Используйте, если эмулятор не запускается с ошибкой "already running"

Write-Host "Очистка lock-файлов эмулятора..." -ForegroundColor Yellow

$avdPath = "$env:USERPROFILE\.android\avd"

if (-not (Test-Path $avdPath)) {
    Write-Host "AVD директория не найдена: $avdPath" -ForegroundColor Red
    exit 1
}

# Находим и удаляем все lock-файлы
$lockFiles = Get-ChildItem -Path $avdPath -Recurse -Filter "*.lock" -ErrorAction SilentlyContinue

if ($lockFiles.Count -eq 0) {
    Write-Host "Lock-файлы не найдены" -ForegroundColor Green
} else {
    Write-Host "Найдено lock-файлов: $($lockFiles.Count)" -ForegroundColor Yellow
    
    foreach ($lock in $lockFiles) {
        try {
            if ($lock.PSIsContainer) {
                Remove-Item -Path $lock.FullName -Recurse -Force
                Write-Host "Удалена директория: $($lock.FullName)" -ForegroundColor Green
            } else {
                Remove-Item -Path $lock.FullName -Force
                Write-Host "Удален файл: $($lock.FullName)" -ForegroundColor Green
            }
        } catch {
            Write-Host "Ошибка при удалении $($lock.FullName): $_" -ForegroundColor Red
        }
    }
}

# Перезапускаем ADB сервер
Write-Host "`nПерезапуск ADB сервера..." -ForegroundColor Yellow
adb kill-server 2>$null
Start-Sleep -Seconds 1
adb start-server 2>$null

Write-Host "`nГотово! Теперь можно запускать эмулятор." -ForegroundColor Green

