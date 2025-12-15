@echo off
chcp 65001 >nul
echo ========================================
echo Удаление автозапуска BTEU Schedule Server
echo ========================================
echo.

REM Удаляем задачу из планировщика заданий
echo Удаление задачи из планировщика заданий...
schtasks /Delete /TN "BTEU_Schedule_Server" /F >nul 2>&1
if errorlevel 1 (
    echo ⚠️  Задача в планировщике не найдена (возможно, уже удалена)
) else (
    echo ✅ Задача удалена из планировщика заданий
)

REM Удаляем файл из папки автозагрузки
set "STARTUP_BAT=%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup\BTEU_Schedule_Server.bat"
if exist "%STARTUP_BAT%" (
    del "%STARTUP_BAT%" >nul 2>&1
    echo ✅ Файл удален из папки автозагрузки
) else (
    echo ⚠️  Файл в папке автозагрузки не найден
)

echo.
echo ========================================
echo Автозапуск отключен!
echo ========================================
echo.
echo Нажмите любую клавишу для выхода...
pause >nul

