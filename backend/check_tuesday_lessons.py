"""Проверка занятий на вторник для группы S-4"""
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
    print("ПРОВЕРКА ЗАНЯТИЙ НА ВТОРНИК ДЛЯ ГРУППЫ S-4")
    print("=" * 80)
    
    # Получаем ID группы S-4
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group_result = cur.fetchone()
    if not group_result:
        print("❌ Группа S-4 не найдена!")
        exit(1)
    group_id = group_result[0]
    
    # Получаем занятия на вторник
    cur.execute("""
        SELECT 
            id,
            subject,
            lesson_type,
            lesson_number,
            week_parity
        FROM lessons
        WHERE group_id = %s AND day_of_week = 2
        ORDER BY lesson_number, week_parity
    """, (group_id,))
    
    results = cur.fetchall()
    
    if results:
        print(f"\nНайдено занятий: {len(results)}\n")
        print(f"{'ID':<6} {'Пара':<6} {'Предмет':<60} {'Тип':<15} {'Четность':<10}")
        print("-" * 100)
        
        for lesson_id, subject, lesson_type, pair, parity in results:
            subject_short = subject[:58] + "..." if len(subject) > 60 else subject
            print(f"{lesson_id:<6} {pair or 0:<6} {subject_short:<60} {lesson_type or 'NULL':<15} {parity or 'NULL':<10}")
        
        # Проверяем конкретные предметы из скриншота
        print("\n" + "=" * 80)
        print("ПРОВЕРКА КОНКРЕТНЫХ ПРЕДМЕТОВ:")
        print("=" * 80)
        
        problem_subjects = [
            'Системы и технологии интеллектуальной обработки данных',
            'Распределенные информационные системы'
        ]
        
        for subject_pattern in problem_subjects:
            cur.execute("""
                SELECT 
                    id,
                    subject,
                    lesson_type,
                    lesson_number
                FROM lessons
                WHERE group_id = %s 
                  AND day_of_week = 2
                  AND subject ILIKE %s
                ORDER BY lesson_number
            """, (group_id, f'%{subject_pattern}%'))
            
            matches = cur.fetchall()
            if matches:
                print(f"\n📚 Предмет: '{subject_pattern}'")
                for lesson_id, subject, lesson_type, pair in matches:
                    print(f"  ID: {lesson_id}, Пара: {pair}")
                    print(f"  Полное название: {subject}")
                    print(f"  Текущий тип: {lesson_type}")
                    
                    # Анализируем регистр
                    uppercase = sum(1 for c in subject if c.isalpha() and c.isupper())
                    lowercase = sum(1 for c in subject if c.isalpha() and c.islower())
                    total = uppercase + lowercase
                    upper_pct = (uppercase / total * 100) if total > 0 else 0
                    print(f"  Заглавных: {uppercase}, Строчных: {lowercase}, Всего: {total}, % заглавных: {upper_pct:.1f}%")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

