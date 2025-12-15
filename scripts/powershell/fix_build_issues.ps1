# Скрипт для исправления проблем сборки Android проекта
# Запуск: .\fix_build_issues.ps1

Write-Host "🔧 Начинаем исправление проблем сборки..." -ForegroundColor Cyan

# Шаг 1: Остановка Gradle daemon
Write-Host "`n1. Останавливаем Gradle daemon..." -ForegroundColor Yellow
& .\gradlew.bat --stop
if ($LASTEXITCODE -ne 0) {
    Write-Host "   ⚠️ Не удалось остановить daemon (возможно, он не запущен)" -ForegroundColor Yellow
}

# Шаг 2: Очистка проекта
Write-Host "`n2. Очищаем проект..." -ForegroundColor Yellow
& .\gradlew.bat clean
if ($LASTEXITCODE -ne 0) {
    Write-Host "   ❌ Ошибка при очистке проекта!" -ForegroundColor Red
    exit 1
}

# Шаг 3: Удаление папок кэша
Write-Host "`n3. Удаляем папки кэша..." -ForegroundColor Yellow
$cacheDirs = @(
    "app\build",
    ".gradle",
    "build",
    "app\.cxx"
)

foreach ($dir in $cacheDirs) {
    if (Test-Path $dir) {
        Write-Host "   Удаляем: $dir" -ForegroundColor Gray
        Remove-Item -Path $dir -Recurse -Force -ErrorAction SilentlyContinue
    }
}

# Шаг 4: Очистка кэша Kotlin
Write-Host "`n4. Очищаем кэш Kotlin..." -ForegroundColor Yellow
$kotlinCache = "$env:USERPROFILE\.kotlin\daemon"
if (Test-Path $kotlinCache) {
    Write-Host "   Очищаем кэш Kotlin daemon..." -ForegroundColor Gray
    Remove-Item -Path "$kotlinCache\*" -Recurse -Force -ErrorAction SilentlyContinue
}

# Шаг 5: Пересборка проекта
Write-Host "`n5. Пересобираем проект..." -ForegroundColor Yellow
& .\gradlew.bat build --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "   ⚠️ Есть ошибки компиляции. Проверьте вывод выше." -ForegroundColor Yellow
} else {
    Write-Host "   ✅ Сборка успешна!" -ForegroundColor Green
}

Write-Host "`n✅ Процесс завершен!" -ForegroundColor Green
Write-Host "`nСледующие шаги:" -ForegroundColor Cyan
Write-Host "1. Откройте Android Studio" -ForegroundColor White
Write-Host "2. File > Invalidate Caches / Restart..." -ForegroundColor White
Write-Host "3. Выберите 'Invalidate and Restart'" -ForegroundColor White
Write-Host "4. После перезапуска попробуйте запустить приложение" -ForegroundColor White

