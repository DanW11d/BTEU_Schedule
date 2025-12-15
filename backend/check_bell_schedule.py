"""Проверка расписания звонков в БД"""
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
    
    print("=" * 60)
    print("ПРОВЕРКА РАСПИСАНИЯ ЗВОНКОВ (BELL_SCHEDULE)")
    print("=" * 60)
    
    cur.execute("""
        SELECT 
            lesson_number,
            lesson_start,
            lesson_end,
            break_time_minutes,
            break_after_lesson_minutes
        FROM bell_schedule
        ORDER BY lesson_number
    """)
    
    bells = cur.fetchall()
    
    if bells:
        print(f"\nНайдено записей: {len(bells)}\n")
        for bell in bells:
            lesson_num, start, end, break_time, break_after = bell
            start_str = str(start)[:5] if start else "не указано"
            end_str = str(end)[:5] if end else "не указано"
            print(f"  Пара {lesson_num}: {start_str} - {end_str}")
            if break_time:
                print(f"    Перемена: {break_time} мин")
    else:
        print("\n❌ Расписание звонков не найдено в БД!")
        print("\nНужно добавить данные в таблицу bell_schedule")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

