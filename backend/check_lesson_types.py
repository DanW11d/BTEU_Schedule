"""Проверка типов занятий после парсинга"""
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
    print("ПРОВЕРКА ТИПОВ ЗАНЯТИЙ")
    print("=" * 80)
    
    # Получаем ID группы
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group = cur.fetchone()
    group_id = group[0]
    
    # Проверяем типы занятий
    print("\nПримеры занятий по типам:\n")
    
    cur.execute("""
        SELECT 
            subject,
            lesson_type,
            CASE 
                WHEN subject ~ '^[А-ЯЁ]' THEN 'Начинается с заглавной'
                ELSE 'Начинается со строчной'
            END as first_letter,
            CASE 
                WHEN subject ~ '[А-ЯЁ]{2,}' THEN 'Содержит много заглавных'
                ELSE 'В основном строчные'
            END as case_pattern
        FROM lessons
        WHERE group_id = %s
        ORDER BY lesson_type, subject
        LIMIT 20
    """, (group_id,))
    
    results = cur.fetchall()
    
    print("Предмет | Тип | Паттерн")
    print("-" * 80)
    
    for subject, lesson_type, first_letter, case_pattern in results:
        subject_preview = subject[:50] if subject else ""
        type_display = {'lecture': 'ЛЕКЦИЯ', 'practice': 'Практика', 'laboratory': 'Лабораторная'}.get(lesson_type, lesson_type)
        print(f"{subject_preview:50s} | {type_display:10s} | {case_pattern}")
    
    # Статистика по типам
    print("\n" + "=" * 80)
    print("СТАТИСТИКА ПО ТИПАМ")
    print("=" * 80)
    
    cur.execute("""
        SELECT 
            lesson_type,
            COUNT(*) as count
        FROM lessons
        WHERE group_id = %s
        GROUP BY lesson_type
        ORDER BY lesson_type
    """, (group_id,))
    
    stats = cur.fetchall()
    
    for lesson_type, count in stats:
        type_display = {'lecture': 'ЛЕКЦИЯ', 'practice': 'Практика', 'laboratory': 'Лабораторная'}.get(lesson_type, lesson_type)
        print(f"{type_display:15s}: {count} занятий")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

