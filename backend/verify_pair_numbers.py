"""Проверка правильности определения номеров пар"""
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
    print("ПРОВЕРКА ПРАВИЛЬНОСТИ ОПРЕДЕЛЕНИЯ НОМЕРОВ ПАР")
    print("=" * 80)
    
    # Получаем ID группы
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group = cur.fetchone()
    group_id = group[0]
    
    # Проверяем соответствие времени и номера пары
    print("\nПроверка соответствия времени и номера пары:\n")
    
    cur.execute("""
        SELECT 
            l.day_of_week,
            l.lesson_number,
            COALESCE(
                TO_CHAR(bs.lesson_start, 'HH24:MI') || '-' || TO_CHAR(bs.lesson_end, 'HH24:MI'),
                ''
            ) as time,
            l.subject,
            l.week_parity
        FROM lessons l
        LEFT JOIN bell_schedule bs ON l.lesson_number = bs.lesson_number
        WHERE l.group_id = %s
        ORDER BY l.day_of_week, l.lesson_number, l.week_parity
    """, (group_id,))
    
    results = cur.fetchall()
    
    days = ['ПН', 'ВТ', 'СР', 'ЧТ', 'ПТ', 'СБ']
    
    # Группируем по дням и парам
    current_day = None
    for day, lesson_num, time, subject, week_parity in results:
        if current_day != day:
            current_day = day
            print(f"\n{'='*80}")
            print(f"{days[day-1]} (День {day})")
            print(f"{'='*80}")
        
        week_name = {'odd': 'нечет', 'even': 'чет', 'both': 'обе'}.get(week_parity, week_parity)
        time_display = time if time else "нет времени"
        subject_preview = subject[:50] if subject else ""
        
        print(f"  Пара {lesson_num:1d} | {time_display:15s} | {week_name:6s} | {subject_preview}")
    
    # Проверяем, есть ли занятия с неправильными номерами пар
    print("\n" + "=" * 80)
    print("ПРОВЕРКА НА ОШИБКИ")
    print("=" * 80)
    
    # Проверяем, что номера пар в диапазоне 1-7
    cur.execute("""
        SELECT COUNT(*) FROM lessons
        WHERE group_id = %s AND (lesson_number < 1 OR lesson_number > 7)
    """, (group_id,))
    
    invalid_numbers = cur.fetchone()[0]
    if invalid_numbers > 0:
        print(f"\n⚠ Найдено {invalid_numbers} занятий с неправильными номерами пар (не 1-7)")
    else:
        print("\n✅ Все номера пар в правильном диапазоне (1-7)")
    
    # Проверяем, есть ли занятия без времени
    cur.execute("""
        SELECT COUNT(*) FROM lessons l
        LEFT JOIN bell_schedule bs ON l.lesson_number = bs.lesson_number
        WHERE l.group_id = %s AND bs.lesson_number IS NULL
    """, (group_id,))
    
    no_time = cur.fetchone()[0]
    if no_time > 0:
        print(f"⚠ Найдено {no_time} занятий без времени в bell_schedule")
    else:
        print("✅ У всех занятий есть время в bell_schedule")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

