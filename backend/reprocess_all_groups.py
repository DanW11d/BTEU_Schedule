"""Переобработка всех групп для исправления разбитых предметов"""
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

def reprocess_all():
    """Переобрабатывает все файлы для исправления разбитых предметов"""
    print("=" * 60)
    print("ПЕРЕОБРАБОТКА ВСЕХ ГРУПП")
    print("=" * 60)
    
    # Получаем список всех групп из БД
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()
        cur.execute("SELECT id, code FROM groups WHERE is_active = TRUE ORDER BY code")
        groups = cur.fetchall()
        cur.close()
        conn.close()
        
        print(f"\nНайдено групп в БД: {len(groups)}")
    except Exception as e:
        print(f"Ошибка получения групп: {e}")
        return
    
    # Получаем список всех файлов
    if not os.path.exists(EXCEL_DIR):
        print(f"\n❌ Директория {EXCEL_DIR} не существует!")
        return
    
    files = [f for f in os.listdir(EXCEL_DIR) 
             if f.endswith('.xls') or f.endswith('.xlsx')]
    
    print(f"Найдено файлов: {len(files)}\n")
    
    # Создаем словарь: код группы -> список файлов
    group_files = {}
    for filename in files:
        group_code, _ = parse_filename(filename)
        if not group_code:
            # Пытаемся извлечь из имени файла
            from file_processor import extract_group_code
            group_code = extract_group_code(filename)
        
        if group_code:
            if group_code not in group_files:
                group_files[group_code] = []
            group_files[group_code].append(filename)
    
    print(f"Найдено уникальных групп в файлах: {len(group_files)}\n")
    
    # Обрабатываем каждую группу
    processed = 0
    for group_code, file_list in sorted(group_files.items()):
        # Проверяем, есть ли группа в БД
        group_in_db = any(g[1] == group_code for g in groups)
        if not group_in_db:
            print(f"⚠ Группа {group_code} не найдена в БД, пропускаем")
            continue
        
        print(f"[{processed + 1}/{len(group_files)}] Группа {group_code}: {len(file_list)} файл(ов)")
        
        # Удаляем старые занятия
        try:
            conn = psycopg2.connect(**DB_CONFIG)
            cur = conn.cursor()
            cur.execute("SELECT id FROM groups WHERE code = %s", (group_code,))
            group = cur.fetchone()
            if group:
                group_id = group[0]
                cur.execute("DELETE FROM lessons WHERE group_id = %s", (group_id,))
                deleted = cur.rowcount
                conn.commit()
                print(f"  Удалено старых занятий: {deleted}")
            cur.close()
            conn.close()
        except Exception as e:
            print(f"  ⚠ Ошибка при удалении: {e}")
        
        # Обрабатываем файлы
        for filename in file_list:
            filepath = os.path.join(EXCEL_DIR, filename)
            try:
                with open(filepath, 'rb') as f:
                    files_data = {'file': (filename, f, 'application/vnd.ms-excel' if filename.endswith('.xls') else 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')}
                    response = requests.post(API_URL, files=files_data, data={'group_code': group_code})
                
                if response.status_code == 200:
                    result = response.json()
                    if result.get('success'):
                        parsed = result.get('parsed_count', 0)
                        saved = result.get('saved_count', 0)
                        if saved > 0:
                            print(f"    ✅ {filename}: сохранено {saved} занятий")
        except Exception as e:
            print(f"    ❌ {filename}: ошибка - {e}")
        
        processed += 1
        print()
    
    print("=" * 60)
    print("ПЕРЕОБРАБОТКА ЗАВЕРШЕНА")
    print("=" * 60)

if __name__ == "__main__":
    reprocess_all()

