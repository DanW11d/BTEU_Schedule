# Скрипт для проверки доступности веб-сервера
# Проверяет различные порты на указанном IP

$ip = "46.56.85.227"
$ports = @(80, 443, 8080, 8000, 3000, 5000)

Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
Write-Host "Проверка доступности веб-сервера" -ForegroundColor Cyan
Write-Host "IP: $ip" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

$found = $false

foreach ($port in $ports) {
    # Проверка HTTP
    try {
        $httpUrl = "http://${ip}:${port}/"
        Write-Host "Проверка HTTP порт $port..." -NoNewline
        
        $response = Invoke-WebRequest -Uri $httpUrl -Method GET -TimeoutSec 5 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host " ✅ ДОСТУПЕН!" -ForegroundColor Green
            Write-Host "   URL: $httpUrl" -ForegroundColor Green
            Write-Host "   Статус: $($response.StatusCode)" -ForegroundColor Green
            Write-Host "   Размер ответа: $($response.Content.Length) байт" -ForegroundColor Green
            $found = $true
        }
    } catch {
        Write-Host " ❌ Недоступен" -ForegroundColor Red
    }
    
    # Проверка HTTPS
    try {
        $httpsUrl = "https://${ip}:${port}/"
        Write-Host "Проверка HTTPS порт $port..." -NoNewline
        
        # Игнорируем ошибки SSL для проверки
        [System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}
        $response = Invoke-WebRequest -Uri $httpsUrl -Method GET -TimeoutSec 5 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host " ✅ ДОСТУПЕН!" -ForegroundColor Green
            Write-Host "   URL: $httpsUrl" -ForegroundColor Green
            Write-Host "   Статус: $($response.StatusCode)" -ForegroundColor Green
            Write-Host "   Размер ответа: $($response.Content.Length) байт" -ForegroundColor Green
            $found = $true
        }
    } catch {
        Write-Host " ❌ Недоступен" -ForegroundColor Red
    }
    
    Write-Host ""
}

Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
if ($found) {
    Write-Host "✅ Веб-сервер найден!" -ForegroundColor Green
    Write-Host "Используйте найденный URL в AppConfig.kt" -ForegroundColor Yellow
} else {
    Write-Host "❌ Веб-сервер не найден на проверенных портах" -ForegroundColor Red
    Write-Host "Рекомендации:" -ForegroundColor Yellow
    Write-Host "1. Уточните у администратора правильный URL для API" -ForegroundColor Yellow
    Write-Host "2. Проверьте другие порты вручную" -ForegroundColor Yellow
    Write-Host "3. Возможно, нужен другой домен (не IP)" -ForegroundColor Yellow
}
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan

# Проверка стандартных путей API
Write-Host ""
Write-Host "Проверка стандартных путей API..." -ForegroundColor Cyan
$apiPaths = @("/api/", "/v1/", "/api/v1/", "/rest/", "/")

foreach ($path in $apiPaths) {
    foreach ($port in @(80, 8080, 8000)) {
        try {
            $url = "http://${ip}:${port}${path}"
            Write-Host "Проверка: $url" -NoNewline
            $response = Invoke-WebRequest -Uri $url -Method GET -TimeoutSec 3 -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                Write-Host " ✅" -ForegroundColor Green
                Write-Host "   Найден API! URL: $url" -ForegroundColor Green
            }
        } catch {
            # Игнорируем ошибки
        }
    }
}

