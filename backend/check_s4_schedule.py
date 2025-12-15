"""Проверка расписания группы S-4 в БД"""
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
    print("ПРОВЕРКА РАСПИСАНИЯ ГРУППЫ S-4")
    print("=" * 60)
    
    # Получаем ID группы
    cur.execute("SELECT id, code, name FROM groups WHERE code = 'S-4'")
    group = cur.fetchone()
    
    if not group:
        print("\n❌ Группа S-4 не найдена в БД!")
        exit(1)
    
    group_id, group_code, group_name = group
    print(f"\nГруппа: {group_code} - {group_name}")
    print(f"ID группы: {group_id}\n")
    
    # Получаем все занятия группы
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
        ORDER BY l.day_of_week, l.lesson_number
    """, (group_id,))
    
    lessons = cur.fetchall()
    
    print(f"Всего занятий в БД: {len(lessons)}\n")
    
    if lessons:
        # Группируем по дням
        days = ['Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота']
        for day_num in range(1, 7):
            day_lessons = [l for l in lessons if l[2] == day_num]
            if day_lessons:
                print(f"\n{days[day_num-1]} (День {day_num}):")
                for lesson in day_lessons:
                    lesson_id, lesson_num, day, subject, teacher, classroom, lesson_type, week_parity = lesson
                    print(f"  Пара {lesson_num}: {subject[:60]}...")
                    print(f"    Преподаватель: {teacher or 'не указан'}")
                    print(f"    Аудитория: {classroom or 'не указана'}")
                    print(f"    Тип: {lesson_type}, Неделя: {week_parity}")
    else:
        print("\n❌ Занятий не найдено!")
    
    # Проверяем дубликаты
    print("\n" + "=" * 60)
    print("ПРОВЕРКА ДУБЛИКАТОВ")
    print("=" * 60)
    
    cur.execute("""
        SELECT 
            lesson_number,
            day_of_week,
            subject,
            week_parity,
            COUNT(*) as count
        FROM lessons
        WHERE group_id = %s
        GROUP BY lesson_number, day_of_week, subject, week_parity
        HAVING COUNT(*) > 1
    """, (group_id,))
    
    duplicates = cur.fetchall()
    
    if duplicates:
        print(f"\n⚠ Найдено дубликатов: {len(duplicates)}")
        for dup in duplicates:
            print(f"  Пара {dup[0]}, День {dup[1]}, Неделя {dup[3]}: {dup[4]} записей")
            print(f"    Предмет: {dup[2][:60]}...")
    else:
        print("\n✅ Дубликатов не найдено")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

