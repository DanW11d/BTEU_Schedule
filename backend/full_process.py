"""
Полный автоматический процесс: скачать → обработать → загрузить в БД
Упрощенная версия для быстрого запуска
"""
import os
import sys
import subprocess
from pathlib import Path
import io

# Устанавливаем UTF-8 для вывода на Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')


def main():
    """Главная функция"""
    # Путь к директории с файлами
    excel_dir = r"D:\Excel file"
    api_url = "http://localhost:8000"
    
    print("=" * 70)
    print("ПОЛНЫЙ АВТОМАТИЧЕСКИЙ ПРОЦЕСС ОБРАБОТКИ РАСПИСАНИЯ")
    print("=" * 70)
    print()
    
    # Шаг 1: Скачивание файлов
    print("Шаг 1: Скачивание файлов с сайта...")
    print(f"  Директория: {excel_dir}")
    print()
    
    try:
        result = subprocess.run(
            [sys.executable, "backend/download_schedule.py", "--dir", excel_dir],
            capture_output=True,
            text=True,
            encoding='utf-8'
        )
        print(result.stdout)
        if result.stderr:
            print(result.stderr)
    except Exception as e:
        print(f"❌ Ошибка скачивания: {e}")
        return
    
    print()
    
    # Шаг 2: Проверка API
    print("Шаг 2: Проверка доступности API...")
    try:
        import requests
        response = requests.get(f"{api_url}/v1/health", timeout=5)
        if response.status_code == 200:
            print(f"✓ API сервер доступен: {api_url}")
        else:
            print(f"❌ API сервер недоступен: {api_url}")
            print("   Запустите сервер: python backend/server.py")
            return
    except Exception as e:
        print(f"❌ API сервер недоступен: {api_url}")
        print("   Запустите сервер: python backend/server.py")
        return
    
    print()
    
    # Шаг 3: Обработка файлов
    print("Шаг 3: Обработка Excel файлов и загрузка в БД...")
    print(f"  Директория: {excel_dir}")
    print()
    
    try:
        result = subprocess.run(
            [sys.executable, "backend/batch_parser.py", "--dir", excel_dir, "--api-url", api_url],
            capture_output=False,  # Показываем вывод в реальном времени
            text=True,
            encoding='utf-8'
        )
        
        if result.returncode == 0:
            print()
            print("=" * 70)
            print("✓ ВСЕ ЭТАПЫ ЗАВЕРШЕНЫ УСПЕШНО!")
            print("=" * 70)
            print()
            print("Данные загружены в БД. Приложение может получать их через API.")
        else:
            print()
            print("⚠ Обработка завершена с ошибками. Проверьте вывод выше.")
    except Exception as e:
        print(f"❌ Ошибка обработки: {e}")


if __name__ == '__main__':
    main()

