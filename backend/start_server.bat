@echo off
chcp 65001 >nul
echo ========================================
echo BTEU Schedule Backend Server
echo ========================================
echo.

REM Проверка наличия Python
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python не найден!
    echo Установите Python с https://www.python.org/
    pause
    exit /b 1
)

REM Проверка наличия .env файла
if not exist .env (
    echo [WARNING] Файл .env не найден!
    echo Создание .env из примера...
    if exist .env.example (
        copy .env.example .env >nul
        echo Файл .env создан!
    ) else (
        echo Создание базового .env файла...
        (
            echo DB_HOST=localhost
            echo DB_PORT=5432
            echo DB_NAME=postgres
            echo DB_USER=postgres
            echo DB_PASSWORD=postgres
        ) > .env
        echo Файл .env создан с настройками по умолчанию!
    )
    echo.
    echo ВАЖНО: Отредактируйте файл .env с правильными настройками БД!
    echo Нажмите любую клавишу для продолжения...
    pause >nul
)

REM Проверка зависимостей
echo Проверка зависимостей...
python -c "import flask" >nul 2>&1
if errorlevel 1 (
    echo [WARNING] Flask не установлен!
    echo Установка зависимостей...
    pip install -r requirements.txt
    if errorlevel 1 (
        echo [ERROR] Не удалось установить зависимости!
        pause
        exit /b 1
    )
)

echo.
echo ========================================
echo Запуск сервера...
echo ========================================
echo.
echo Сервер будет доступен на:
echo   - http://localhost:8000 (с компьютера)
echo   - http://10.0.2.2:8000 (из Android эмулятора)
echo.
echo Для остановки нажмите Ctrl+C
echo ========================================
echo.

python server.py

if errorlevel 1 (
    echo.
    echo [ERROR] Сервер завершился с ошибкой!
    echo Проверьте:
    echo   1. PostgreSQL запущен
    echo   2. Настройки в .env файле правильные
    echo   3. База данных существует
    pause
)

