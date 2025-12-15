@echo off
REM Простая проверка FTP через командную строку Windows
REM Запустите: test_ftp.bat

echo Проверка FTP подключения...
echo.
echo Сервер: 46.56.85.227:21
echo Пользователь: rasp
echo Путь: /raspisanie
echo.

(
echo open 46.56.85.227 21
echo rasp
echo psar_25
echo cd /raspisanie
echo ls
echo quit
) | ftp -n

echo.
echo Проверка завершена.
pause

