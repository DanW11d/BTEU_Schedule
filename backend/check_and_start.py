"""
Скрипт для проверки и запуска сервера
"""
import sys
import socket
import subprocess
import os
from pathlib import Path

def check_port(port):
    """Проверяет, занят ли порт"""
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    result = sock.connect_ex(('localhost', port))
    sock.close()
    return result == 0

def check_env_file():
    """Проверяет наличие .env файла"""
    env_path = Path('.env')
    if not env_path.exists():
        print("❌ Файл .env не найден!")
        print("Создайте файл .env на основе .env.example")
        return False
    
    # Проверяем, что файл не пустой
    with open('.env', 'r') as f:
        content = f.read().strip()
        if not content or 'DB_PASSWORD=postgres' in content:
            print("⚠️  ВНИМАНИЕ: Проверьте настройки в .env файле!")
            print("   Убедитесь, что DB_PASSWORD установлен правильно")
    
    return True

def check_database():
    """Проверяет подключение к БД"""
    try:
        import psycopg2
        from dotenv import load_dotenv
        
        load_dotenv()
        
        DB_CONFIG = {
            'host': os.getenv('DB_HOST', 'localhost'),
            'port': os.getenv('DB_PORT', '5432'),
            'database': os.getenv('DB_NAME', 'postgres'),
            'user': os.getenv('DB_USER', 'postgres'),
            'password': os.getenv('DB_PASSWORD', 'postgres')
        }
        
        conn = psycopg2.connect(**DB_CONFIG)
        conn.close()
        print("✅ Подключение к БД успешно")
        return True
    except ImportError:
        print("❌ psycopg2 не установлен. Выполните: pip install -r requirements.txt")
        return False
    except Exception as e:
        print(f"❌ Ошибка подключения к БД: {e}")
        print("\nПроверьте:")
        print("1. PostgreSQL запущен")
        print("2. Настройки в .env файле правильные")
        print("3. База данных существует")
        return False

def main():
    print("=" * 60)
    print("Проверка готовности к запуску сервера")
    print("=" * 60)
    print()
    
    # Проверка .env файла
    if not check_env_file():
        return
    
    # Проверка порта
    port = 8000
    if check_port(port):
        print(f"⚠️  Порт {port} уже занят!")
        print("   Возможно, сервер уже запущен")
        print("   Или другой процесс использует этот порт")
        response = input("\nПродолжить запуск? (y/n): ")
        if response.lower() != 'y':
            return
    else:
        print(f"✅ Порт {port} свободен")
    
    # Проверка БД
    if not check_database():
        print("\n⚠️  Не удалось подключиться к БД")
        print("   Сервер все равно будет запущен, но API может не работать")
        response = input("\nПродолжить запуск? (y/n): ")
        if response.lower() != 'y':
            return
    
    print()
    print("=" * 60)
    print("Запуск сервера...")
    print("=" * 60)
    print()
    print("Сервер будет доступен на:")
    print("  - http://localhost:8000 (с компьютера)")
    print("  - http://10.0.2.2:8000 (из Android эмулятора)")
    print()
    print("Для остановки нажмите Ctrl+C")
    print("=" * 60)
    print()
    
    # Запуск сервера
    try:
        subprocess.run([sys.executable, 'server.py'], check=True)
    except KeyboardInterrupt:
        print("\n\nСервер остановлен")
    except Exception as e:
        print(f"\n❌ Ошибка запуска: {e}")

if __name__ == '__main__':
    main()

