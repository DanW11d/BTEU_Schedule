"""Очистка и переобработка файлов S-4"""
import os
import sys
import io
import psycopg2
import requests
from dotenv import load_dotenv
from file_processor import parse_filename

# Исправление кодировки для Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

load_dotenv()

DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': int(os.getenv('DB_PORT', '5432')),
    'database': os.getenv('DB_NAME', 'postgres'),
    'user': os.getenv('DB_USER', 'postgres'),
    'password': os.getenv('DB_PASSWORD', '7631')
}

API_URL = "http://localhost:8000/v1/admin/parse-excel"
EXCEL_DIR = r"D:\Excel file"

def clean_and_reprocess():
    """Удаляет старые занятия S-4 и переобрабатывает файлы"""
    print("=" * 60)
    print("ОЧИСТКА И ПЕРЕОБРАБОТКА ГРУППЫ S-4")
    print("=" * 60)
    
    # 1. Удаляем старые занятия
    print("\n1. Удаление старых занятий группы S-4...")
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()
        
        # Получаем ID группы S-4
        cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
        group = cur.fetchone()
        
        if not group:
            print("   ❌ Группа S-4 не найдена!")
            cur.close()
            conn.close()
            return
        
        group_id = group[0]
        
        # Удаляем занятия
        cur.execute("DELETE FROM lessons WHERE group_id = %s", (group_id,))
        deleted_count = cur.rowcount
        conn.commit()
        
        cur.close()
        conn.close()
        
        print(f"   ✅ Удалено занятий: {deleted_count}")
    except Exception as e:
        print(f"   ❌ Ошибка при удалении: {e}")
        return
    
    # 2. Переобрабатываем файлы
    print("\n2. Переобработка файлов S-4...")
    
    if not os.path.exists(EXCEL_DIR):
        print(f"   ❌ Директория {EXCEL_DIR} не существует!")
        return
    
    files = [f for f in os.listdir(EXCEL_DIR) 
             if f.lower().startswith('s-4') and (f.endswith('.xls') or f.endswith('.xlsx'))]
    
    if not files:
        print(f"   ❌ Файлы S-4 не найдены в {EXCEL_DIR}")
        return
    
    print(f"   Найдено файлов: {len(files)}\n")
    
    for filename in files:
        filepath = os.path.join(EXCEL_DIR, filename)
        print(f"   [{files.index(filename) + 1}/{len(files)}] {filename}")
        
        group_code = 'S-4'
        _, modification_date = parse_filename(filename)
        
        if modification_date:
            print(f"      Дата: {modification_date}")
        
        # Отправляем на парсинг
        try:
            with open(filepath, 'rb') as f:
                files_data = {'file': (filename, f, 'application/vnd.ms-excel' if filename.endswith('.xls') else 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')}
                response = requests.post(API_URL, files=files_data, data={'group_code': group_code})
            
            if response.status_code == 200:
                result = response.json()
                if result.get('success'):
                    parsed = result.get('parsed_count', 0)
                    saved = result.get('saved_count', 0)
                    print(f"      ✅ Распарсено: {parsed}, сохранено: {saved}")
                else:
                    print(f"      [!] Занятий не найдено")
            else:
                error = response.json().get('error', 'Неизвестная ошибка')
                print(f"      [X] Ошибка: {error}")
        except Exception as e:
            print(f"      [X] Ошибка: {e}")
    
    print("\n" + "=" * 60)
    print("ГОТОВО! Проверьте расписание в приложении.")
    print("=" * 60)

if __name__ == "__main__":
    clean_and_reprocess()

