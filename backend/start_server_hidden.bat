@echo off
chcp 65001 >nul
cd /d "C:\Users\danwi\AndroidStudioProjects\BTEU_Schedule\backend"

REM Добавляем текущую дату и время в лог
echo ======================================== >> "C:\Users\danwi\AndroidStudioProjects\BTEU_Schedule\backend\server.log"
echo [%date% %time%] Запуск сервера >> "C:\Users\danwi\AndroidStudioProjects\BTEU_Schedule\backend\server.log"
echo ======================================== >> "C:\Users\danwi\AndroidStudioProjects\BTEU_Schedule\backend\server.log"

REM Проверяем наличие server.py
if not exist "server.py" (
    echo [%date% %time%] [ERROR] Файл server.py не найден! >> "C:\Users\danwi\AndroidStudioProjects\BTEU_Schedule\backend\server.log"
    exit /b 1
)

REM Запускаем сервер и перенаправляем вывод в лог
"C:\Users\danwi\AppData\Local\Microsoft\WindowsApps\python.exe" "C:\Users\danwi\AndroidStudioProjects\BTEU_Schedule\backend\server.py" >> "C:\Users\danwi\AndroidStudioProjects\BTEU_Schedule\backend\server.log" 2>&1

REM Если сервер завершился с ошибкой, записываем в лог
if errorlevel 1 (
    echo [%date% %time%] [ERROR] Сервер завершился с ошибкой! >> "C:\Users\danwi\AndroidStudioProjects\BTEU_Schedule\backend\server.log"
)
