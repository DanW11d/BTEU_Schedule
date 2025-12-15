"""Проверка типов занятий для группы S-4"""
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
    print("ПРОВЕРКА ТИПОВ ЗАНЯТИЙ ДЛЯ ГРУППЫ S-4")
    print("=" * 80)
    
    # Получаем ID группы
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group_result = cur.fetchone()
    if not group_result:
        print("❌ Группа S-4 не найдена!")
        exit(1)
    group_id = group_result[0]
    
    # Получаем все занятия для группы S-4
    cur.execute("""
        SELECT 
            l.id,
            l.subject,
            l.lesson_type,
            l.day_of_week,
            l.lesson_number
        FROM lessons l
        WHERE l.group_id = %s
        ORDER BY l.day_of_week, l.lesson_number
        LIMIT 50
    """, (group_id,))
    
    results = cur.fetchall()
    
    if results:
        print(f"\nНайдено занятий: {len(results)}\n")
        print(f"{'ID':<6} {'Предмет':<60} {'Тип':<15} {'День':<10} {'Пара':<6}")
        print("-" * 100)
        
        for lesson_id, subject, lesson_type, day_of_week, lesson_number in results:
            subject_short = subject[:58] + "..." if len(subject) > 60 else subject
            day_name = ['ПН', 'ВТ', 'СР', 'ЧТ', 'ПТ', 'СБ'][day_of_week - 1] if day_of_week <= 6 else '?'
            print(f"{lesson_id:<6} {subject_short:<60} {lesson_type or 'NULL':<15} {day_name:<10} {lesson_number or 0:<6}")
        
        # Проверяем конкретные проблемные предметы
        print("\n" + "=" * 80)
        print("ПРОВЕРКА КОНКРЕТНЫХ ПРЕДМЕТОВ")
        print("=" * 80)
        
        problem_subjects = [
            'Интернет-маркетинг',
            'МЕТРОЛОГИЯ',
            'Интеллектуальные информационные системы'
        ]
        
        for subject_pattern in problem_subjects:
            cur.execute("""
                SELECT 
                    subject,
                    lesson_type,
                    COUNT(*) as count
                FROM lessons
                WHERE group_id = %s AND subject ILIKE %s
                GROUP BY subject, lesson_type
                ORDER BY subject
            """, (group_id, f'%{subject_pattern}%'))
            
            matches = cur.fetchall()
            if matches:
                print(f"\n📚 Предметы содержащие '{subject_pattern}':")
                for subj, ltype, cnt in matches:
                    subj_short = subj[:70] + "..." if len(subj) > 70 else subj
                    print(f"  - {subj_short}")
                    print(f"    Тип: {ltype or 'NULL'}, Количество: {cnt}")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

