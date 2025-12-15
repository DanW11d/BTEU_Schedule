"""Проверка что возвращает API для понедельника, пара 1"""
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
    print("ПРОВЕРКА API ОТВЕТА: ПОНЕДЕЛЬНИК, ПАРА 1, НЕЧЕТНАЯ НЕДЕЛЯ")
    print("=" * 80)
    
    # Получаем ID группы S-4
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group_result = cur.fetchone()
    if not group_result:
        print("❌ Группа S-4 не найдена!")
        exit(1)
    group_id = group_result[0]
    
    # Запрос как в API
    cur.execute("""
        SELECT 
            l.id,
            l.lesson_number,
            l.day_of_week,
            l.subject,
            l.teacher,
            l.classroom,
            l.lesson_type,
            l.week_parity
        FROM lessons l
        WHERE l.group_id = %s 
          AND l.day_of_week = 1 
          AND l.week_parity = 'odd'
        ORDER BY l.lesson_number
    """, (group_id,))
    
    results = cur.fetchall()
    
    if results:
        print(f"\nНайдено занятий: {len(results)}\n")
        print(f"{'ID':<6} {'Пара':<6} {'Предмет':<60} {'Тип':<15}")
        print("-" * 90)
        
        for lesson_id, pair_num, day, subject, teacher, classroom, lesson_type, parity in results:
            subject_short = subject[:58] + "..." if len(subject) > 60 else subject
            print(f"{lesson_id:<6} {pair_num or 0:<6} {subject_short:<60} {lesson_type or 'NULL':<15}")
        
        # Проверяем конкретно пару 1
        print("\n" + "=" * 80)
        print("ПАРА 1 (lesson_number = 1):")
        print("=" * 80)
        
        pair1_lessons = [r for r in results if r[1] == 1]
        if pair1_lessons:
            for lesson_id, pair_num, day, subject, teacher, classroom, lesson_type, parity in pair1_lessons:
                print(f"\nID: {lesson_id}")
                print(f"  Предмет: {subject}")
                print(f"  Тип: {lesson_type}")
                print(f"  Преподаватель: {teacher}")
                print(f"  Аудитория: {classroom}")
                print(f"  Четность: {parity}")
        else:
            print("❌ Занятий для пары 1 не найдено!")
    else:
        print("❌ Занятий не найдено!")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

