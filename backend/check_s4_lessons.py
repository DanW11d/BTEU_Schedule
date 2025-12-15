"""Проверка занятий группы S-4 в БД"""
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
    print("ПРОВЕРКА ЗАНЯТИЙ ГРУППЫ S-4")
    print("=" * 60)
    
    # Получаем ID группы S-4
    cur.execute("SELECT id, code FROM groups WHERE code = 'S-4'")
    group = cur.fetchone()
    
    if not group:
        print("\n❌ Группа S-4 не найдена!")
        cur.close()
        conn.close()
        exit(1)
    
    group_id, group_code = group
    print(f"\nГруппа: {group_code} (ID: {group_id})")
    
    # Получаем все занятия группы S-4 на понедельник (day_of_week = 1)
    cur.execute("""
        SELECT 
            id,
            day_of_week,
            lesson_number,
            subject,
            teacher,
            classroom,
            lesson_type,
            week_parity
        FROM lessons
        WHERE group_id = %s AND day_of_week = 1 AND is_active = TRUE
        ORDER BY lesson_number, id
    """, (group_id,))
    
    lessons = cur.fetchall()
    
    if not lessons:
        print("\n❌ Занятий для группы S-4 на понедельник не найдено!")
    else:
        print(f"\nНайдено занятий на понедельник: {len(lessons)}\n")
        
        current_lesson_num = None
        for lesson in lessons:
            lesson_id, day, lesson_num, subject, teacher, classroom, lesson_type, week_parity = lesson
            
            # Проверяем проблемные случаи
            issues = []
            if lesson_num == 0:
                issues.append("⚠ Пара 0 (должно быть 1-7)")
            if not subject or subject.strip() == "":
                issues.append("⚠ Пустое название предмета")
            if len(subject) < 5:
                issues.append(f"⚠ Слишком короткое название: '{subject}'")
            if subject and subject.startswith("(") or subject and subject.endswith(")"):
                issues.append(f"⚠ Название начинается/заканчивается на скобку: '{subject}'")
            
            if current_lesson_num != lesson_num:
                print(f"\n{'='*60}")
                print(f"ПАРА {lesson_num} (ID: {lesson_id})")
                if issues:
                    print(f"  ПРОБЛЕМЫ: {', '.join(issues)}")
                current_lesson_num = lesson_num
            
            print(f"  ID: {lesson_id}")
            print(f"  Предмет: '{subject}'")
            print(f"  Преподаватель: '{teacher or 'не указан'}'")
            print(f"  Аудитория: '{classroom or 'не указана'}'")
            print(f"  Тип: {lesson_type}")
            print(f"  Неделя: {week_parity}")
            print(f"  Длина названия: {len(subject) if subject else 0} символов")
            print("-" * 60)
    
    # Проверяем статистику
    print("\n" + "=" * 60)
    print("СТАТИСТИКА")
    print("=" * 60)
    
    cur.execute("""
        SELECT 
            COUNT(*) as total,
            COUNT(CASE WHEN lesson_number = 0 THEN 1 END) as zero_par,
            COUNT(CASE WHEN lesson_number < 1 OR lesson_number > 7 THEN 1 END) as invalid_par,
            COUNT(CASE WHEN subject IS NULL OR subject = '' THEN 1 END) as empty_subject,
            COUNT(CASE WHEN LENGTH(subject) < 10 THEN 1 END) as short_subject
        FROM lessons
        WHERE group_id = %s AND is_active = TRUE
    """, (group_id,))
    
    stats = cur.fetchone()
    total, zero_par, invalid_par, empty_subject, short_subject = stats
    
    print(f"\nВсего занятий: {total}")
    print(f"  Пара 0: {zero_par}")
    print(f"  Невалидный номер пары (<1 или >7): {invalid_par}")
    print(f"  Пустое название предмета: {empty_subject}")
    print(f"  Короткое название (<10 символов): {short_subject}")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

