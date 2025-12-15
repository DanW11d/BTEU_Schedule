# Скрипт для проверки FTP подключения
# Запустите в PowerShell: .\test_ftp.ps1

$ftpServer = "46.56.85.227"
$ftpPort = 21
$ftpUser = "rasp"
$ftpPass = "psar_25"
$ftpPath = "/raspisanie"

Write-Host "Проверка FTP подключения..." -ForegroundColor Cyan
Write-Host "Сервер: $ftpServer`:$ftpPort" -ForegroundColor Yellow
Write-Host "Пользователь: $ftpUser" -ForegroundColor Yellow
Write-Host "Путь: $ftpPath" -ForegroundColor Yellow
Write-Host ""

try {
    # Создаем FTP запрос
    $ftpRequest = [System.Net.FtpWebRequest]::Create("ftp://$ftpServer$ftpPath")
    $ftpRequest.Credentials = New-Object System.Net.NetworkCredential($ftpUser, $ftpPass)
    $ftpRequest.Method = [System.Net.WebRequestMethods+Ftp]::ListDirectory
    $ftpRequest.UsePassive = $true
    $ftpRequest.UseBinary = $true
    $ftpRequest.Timeout = 10000
    
    Write-Host "Подключение к серверу..." -ForegroundColor Cyan
    $response = $ftpRequest.GetResponse()
    $responseStream = $response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($responseStream)
    $files = $reader.ReadToEnd()
    
    Write-Host "Успешно подключено!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Список файлов:" -ForegroundColor Cyan
    $files -split "`r`n" | Where-Object { $_ -ne "" } | ForEach-Object {
        Write-Host "  - $_" -ForegroundColor White
    }
    
    $reader.Close()
    $response.Close()
    
    Write-Host ""
    Write-Host "Проверка завершена успешно!" -ForegroundColor Green
} catch {
    Write-Host "Ошибка подключения:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host ""
    Write-Host "Проверьте:" -ForegroundColor Yellow
    Write-Host "  - Доступность сервера $ftpServer`:$ftpPort" -ForegroundColor Yellow
    Write-Host "  - Правильность логина и пароля" -ForegroundColor Yellow
    Write-Host "  - Правильность пути $ftpPath" -ForegroundColor Yellow
    Write-Host "  - Настройки файрвола" -ForegroundColor Yellow
}

