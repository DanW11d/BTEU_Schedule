@echo off
chcp 65001 >nul
echo ========================================
echo Настройка автозапуска (простой способ)
echo ========================================
echo.

REM Получаем путь к текущей директории
set "BACKEND_DIR=%~dp0"
set "STARTUP_DIR=%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup"

echo Текущая директория: %BACKEND_DIR%
echo Папка автозагрузки: %STARTUP_DIR%
echo.

REM Создаем bat-файл для автозапуска
set "STARTUP_BAT=%STARTUP_DIR%\BTEU_Schedule_Server.bat"

(
echo @echo off
echo chcp 65001 ^>nul
echo cd /d "%BACKEND_DIR%"
echo start /min "" python server.py
) > "%STARTUP_BAT%"

if exist "%STARTUP_BAT%" (
    echo ✅ Файл автозапуска создан: %STARTUP_BAT%
    echo.
    echo Сервер будет запускаться автоматически при входе в Windows
    echo.
    echo Для отключения автозапуска удалите файл:
    echo   %STARTUP_BAT%
) else (
    echo ❌ Ошибка создания файла автозапуска
)

echo.
echo Нажмите любую клавишу для выхода...
pause >nul

