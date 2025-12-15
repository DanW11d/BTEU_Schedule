"""Проверка наличия группы S-4 в БД"""
import psycopg2
from dotenv import load_dotenv
import os
import sys
import io

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

try:
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()
    
    # Ищем группы с кодом S или С (кириллица)
    print("=" * 60)
    print("ПОИСК ГРУПП С КОДОМ S или С")
    print("=" * 60)
    
    # Ищем S-4 (латиница)
    cur.execute("""
        SELECT code, name, course, education_form, faculty_id, is_active, specialization
        FROM groups 
        WHERE code LIKE 'S%' OR code LIKE 'С%' OR code LIKE 's%' OR code LIKE 'с%'
        ORDER BY code
    """)
    rows = cur.fetchall()
    
    if rows:
        print(f"\nНайдено групп: {len(rows)}\n")
        for row in rows:
            code, name, course, form, faculty_id, is_active, spec = row
            print(f"  Код: {code}")
            print(f"  Название: {name}")
            print(f"  Курс: {course}")
            print(f"  Форма: {form}")
            print(f"  Специализация: {spec}")
            print(f"  Факультет ID: {faculty_id}")
            print(f"  Активна: {is_active}")
            print("-" * 60)
    else:
        print("\n❌ Группы с кодом S или С не найдены в БД!")
        print("\nПроверяем все группы 4 курса...")
        
        cur.execute("""
            SELECT code, name, course, education_form, is_active
            FROM groups 
            WHERE course = 4 AND is_active = TRUE
            ORDER BY code
        """)
        rows_4 = cur.fetchall()
        
        if rows_4:
            print(f"\nНайдено групп 4 курса: {len(rows_4)}\n")
            for row in rows_4:
                print(f"  {row[0]} - {row[1]} (форма: {row[3]})")
        else:
            print("\n❌ Групп 4 курса не найдено!")
    
    # Проверяем, есть ли в файлах Excel группа S-4
    print("\n" + "=" * 60)
    print("ПРОВЕРКА ФАЙЛОВ EXCEL")
    print("=" * 60)
    
    import os
    excel_dir = r"D:\Excel file"
    if os.path.exists(excel_dir):
        files = [f for f in os.listdir(excel_dir) if 's-4' in f.lower() or 'с-4' in f.lower()]
        if files:
            print(f"\nНайдено файлов с S-4: {len(files)}")
            for f in files[:5]:  # Показываем первые 5
                print(f"  - {f}")
        else:
            print("\n❌ Файлов с S-4 не найдено в D:\\Excel file")
    else:
        print(f"\n⚠ Директория {excel_dir} не существует")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"Ошибка: {e}")
    import traceback
    traceback.print_exc()

