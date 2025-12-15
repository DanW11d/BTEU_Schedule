"""Проверка конкретного предмета в базе данных"""
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
    print("ПРОВЕРКА ПРЕДМЕТА 'Интернет-маркетинг'")
    print("=" * 80)
    
    # Получаем ID группы
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group_result = cur.fetchone()
    if not group_result:
        print("❌ Группа S-4 не найдена!")
        exit(1)
    group_id = group_result[0]
    
    # Ищем предмет "Интернет-маркетинг"
    cur.execute("""
        SELECT 
            id,
            subject,
            lesson_type,
            day_of_week,
            lesson_number,
            teacher,
            classroom
        FROM lessons
        WHERE group_id = %s AND subject ILIKE '%интернет%маркетинг%'
        ORDER BY day_of_week, lesson_number
    """, (group_id,))
    
    results = cur.fetchall()
    
    print(f"\nНайдено записей: {len(results)}\n")
    
    for lesson_id, subject, lesson_type, day, pair, teacher, classroom in results:
        days = ['ПН', 'ВТ', 'СР', 'ЧТ', 'ПТ', 'СБ']
        day_name = days[day - 1] if 1 <= day <= 6 else f"День {day}"
        type_display = {'lecture': 'ЛЕКЦИЯ', 'practice': 'Практика', 'laboratory': 'Лабораторная'}.get(lesson_type, lesson_type)
        
        # Проверяем регистр букв
        uppercase_count = sum(1 for c in subject if c.isupper() and c.isalpha())
        lowercase_count = sum(1 for c in subject if c.islower() and c.isalpha())
        total_letters = uppercase_count + lowercase_count
        
        if total_letters > 0:
            uppercase_percentage = (uppercase_count / total_letters) * 100
        else:
            uppercase_percentage = 0
        
        print(f"ID: {lesson_id}")
        print(f"  Предмет: {subject}")
        print(f"  Тип в БД: {type_display}")
        print(f"  День: {day_name}, Пара: {pair}")
        print(f"  Преподаватель: {teacher}")
        print(f"  Аудитория: {classroom}")
        print(f"  Заглавных букв: {uppercase_count}, Строчных: {lowercase_count}, Процент заглавных: {uppercase_percentage:.1f}%")
        
        # Определяем, какой должен быть тип
        if uppercase_percentage > 50:
            expected_type = 'lecture'
        elif total_letters > 1:
            remaining_uppercase = max(0, uppercase_count - 1) if subject[0].isupper() else uppercase_count
            remaining_total = total_letters - 1
            if remaining_total > 0:
                remaining_uppercase_percentage = (remaining_uppercase / remaining_total) * 100
                if remaining_uppercase_percentage > 20:
                    expected_type = 'lecture'
                else:
                    expected_type = 'practice'
            else:
                expected_type = 'practice'
        else:
            expected_type = 'practice'
        
        expected_display = {'lecture': 'ЛЕКЦИЯ', 'practice': 'Практика'}.get(expected_type, expected_type)
        
        if lesson_type != expected_type:
            print(f"  ⚠ ОШИБКА: Должен быть тип '{expected_display}', но в БД '{type_display}'")
        else:
            print(f"  ✅ Тип правильный: {type_display}")
        print()
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

