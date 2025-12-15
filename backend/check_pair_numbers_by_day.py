"""Проверка номеров пар по дням"""
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
    
    print("=" * 80)
    print("ПРОВЕРКА НОМЕРОВ ПАР ПО ДНЯМ")
    print("=" * 80)
    
    # Получаем ID группы
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group_result = cur.fetchone()
    if not group_result:
        print("❌ Группа S-4 не найдена!")
        exit(1)
    group_id = group_result[0]
    
    days = ['Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота']
    
    for day_num in range(1, 7):
        cur.execute("""
            SELECT 
                lesson_number,
                COUNT(*) as count,
                STRING_AGG(DISTINCT subject, ', ' ORDER BY subject) as subjects
            FROM lessons
            WHERE group_id = %s AND day_of_week = %s AND week_parity = 'odd'
            GROUP BY lesson_number
            ORDER BY lesson_number
        """, (group_id, day_num))
        
        results = cur.fetchall()
        
        if results:
            print(f"\n{days[day_num - 1]} (День {day_num}):")
            print("-" * 80)
            
            for pair_num, count, subjects in results:
                subjects_preview = subjects[:60] if subjects else ""
                print(f"  Пара {pair_num}: {count} занятий - {subjects_preview}...")
            
            # Проверяем, есть ли дубликаты номеров пар
            pair_numbers = [r[0] for r in results]
            if len(pair_numbers) != len(set(pair_numbers)):
                print(f"  ⚠ ВНИМАНИЕ: Есть дубликаты номеров пар!")
            else:
                print(f"  ✅ Уникальные номера пар: {sorted(pair_numbers)}")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

