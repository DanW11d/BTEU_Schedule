"""Проверка дубликатов для группы S-4"""
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
    print("ПРОВЕРКА ДУБЛИКАТОВ ДЛЯ ГРУППЫ S-4")
    print("=" * 60)
    
    # Получаем ID группы
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group = cur.fetchone()
    
    if not group:
        print("\n❌ Группа S-4 не найдена!")
        exit(1)
    
    group_id = group[0]
    
    # Проверяем дубликаты для понедельника четной недели
    print("\nПонедельник, четная неделя:")
    cur.execute("""
        SELECT 
            id,
            lesson_number,
            subject,
            teacher,
            classroom
        FROM lessons
        WHERE group_id = %s 
            AND day_of_week = 1
            AND week_parity = 'even'
        ORDER BY lesson_number
    """, (group_id,))
    
    lessons = cur.fetchall()
    
    print(f"Найдено занятий: {len(lessons)}\n")
    
    for lesson in lessons:
        lesson_id, lesson_num, subject, teacher, classroom = lesson
        print(f"ID: {lesson_id}, Пара: {lesson_num}")
        print(f"  Предмет: {subject[:60]}...")
        print(f"  Преподаватель: {teacher or 'не указан'}")
        print(f"  Аудитория: {classroom or 'не указана'}")
        print()
    
    # Проверяем, есть ли одинаковые занятия
    print("=" * 60)
    print("ПРОВЕРКА ОДИНАКОВЫХ ЗАНЯТИЙ")
    print("=" * 60)
    
    cur.execute("""
        SELECT 
            lesson_number,
            subject,
            teacher,
            classroom,
            COUNT(*) as count
        FROM lessons
        WHERE group_id = %s 
            AND day_of_week = 1
            AND week_parity = 'even'
        GROUP BY lesson_number, subject, teacher, classroom
        HAVING COUNT(*) > 1
    """, (group_id,))
    
    duplicates = cur.fetchall()
    
    if duplicates:
        print(f"\n⚠ Найдено дубликатов: {len(duplicates)}")
        for dup in duplicates:
            print(f"  Пара {dup[0]}: {dup[4]} записей")
            print(f"    Предмет: {dup[1][:60]}...")
    else:
        print("\n✅ Одинаковых занятий не найдено")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

