"""Проверка распределения занятий по неделям"""
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
    print("РАСПРЕДЕЛЕНИЕ ЗАНЯТИЙ ПО НЕДЕЛЯМ ДЛЯ ГРУППЫ S-4")
    print("=" * 60)
    
    # Получаем ID группы
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group = cur.fetchone()
    group_id = group[0]
    
    # Группируем по дням и неделям
    cur.execute("""
        SELECT 
            day_of_week,
            week_parity,
            COUNT(*) as count,
            STRING_AGG(DISTINCT subject, ', ' ORDER BY subject) as subjects
        FROM lessons
        WHERE group_id = %s
        GROUP BY day_of_week, week_parity
        ORDER BY day_of_week, week_parity
    """, (group_id,))
    
    results = cur.fetchall()
    
    days = ['Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота']
    
    print("\nРаспределение по дням и неделям:\n")
    
    for day, week_parity, count, subjects in results:
        day_name = days[day - 1] if 1 <= day <= 6 else f"День {day}"
        week_name = {'odd': 'Нечетная', 'even': 'Четная', 'both': 'Обе'}.get(week_parity, week_parity)
        print(f"{day_name}, {week_name}: {count} занятий")
        # Показываем первые 2 предмета
        subjects_list = subjects.split(', ')
        if len(subjects_list) > 2:
            print(f"  Предметы: {', '.join(subjects_list[:2])}...")
        else:
            print(f"  Предметы: {subjects}")
        print()
    
    # Проверяем, есть ли одинаковые предметы в разные недели
    print("=" * 60)
    print("ПРОВЕРКА ОДИНАКОВЫХ ПРЕДМЕТОВ В РАЗНЫЕ НЕДЕЛИ")
    print("=" * 60)
    
    cur.execute("""
        SELECT 
            day_of_week,
            lesson_number,
            subject,
            COUNT(DISTINCT week_parity) as parity_count,
            STRING_AGG(DISTINCT week_parity::text, ', ') as parities
        FROM lessons
        WHERE group_id = %s
        GROUP BY day_of_week, lesson_number, subject
        HAVING COUNT(DISTINCT week_parity) > 1
        ORDER BY day_of_week, lesson_number
    """, (group_id,))
    
    duplicates = cur.fetchall()
    
    if duplicates:
        print(f"\n⚠ Найдено предметов, которые идут в разные недели: {len(duplicates)}\n")
        for day, lesson_num, subject, parity_count, parities in duplicates:
            day_name = days[day - 1] if 1 <= day <= 6 else f"День {day}"
            print(f"{day_name}, Пара {lesson_num}: {subject[:50]}...")
            print(f"  Недели: {parities}")
    else:
        print("\n✅ Одинаковых предметов в разные недели не найдено")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

