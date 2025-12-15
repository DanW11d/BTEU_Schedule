"""Проверка всех записей с Интернет-маркетинг"""
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
    print("ВСЕ ЗАПИСИ С 'ИНТЕРНЕТ-МАРКЕТИНГ' ДЛЯ ГРУППЫ S-4")
    print("=" * 80)
    
    # Получаем ID группы S-4
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group_result = cur.fetchone()
    if not group_result:
        print("❌ Группа S-4 не найдена!")
        exit(1)
    group_id = group_result[0]
    
    # Получаем все записи с интернет-маркетинг
    cur.execute("""
        SELECT 
            id,
            subject,
            lesson_type,
            day_of_week,
            lesson_number,
            week_parity
        FROM lessons
        WHERE group_id = %s AND subject ILIKE '%интернет%маркетинг%'
        ORDER BY day_of_week, lesson_number, week_parity
    """, (group_id,))
    
    results = cur.fetchall()
    
    if results:
        print(f"\nНайдено записей: {len(results)}\n")
        print(f"{'ID':<6} {'Предмет':<50} {'Тип':<15} {'День':<6} {'Пара':<6} {'Четность':<10}")
        print("-" * 100)
        
        for lesson_id, subject, lesson_type, day, pair, parity in results:
            subject_short = subject[:48] + "..." if len(subject) > 50 else subject
            day_name = ['ПН', 'ВТ', 'СР', 'ЧТ', 'ПТ', 'СБ'][day - 1] if day <= 6 else '?'
            print(f"{lesson_id:<6} {subject_short:<50} {lesson_type or 'NULL':<15} {day_name:<6} {pair or 0:<6} {parity or 'NULL':<10}")
        
        # Проверяем, есть ли записи с маленькими буквами, но типом lecture
        print("\n" + "=" * 80)
        print("ПРОВЕРКА: ЗАПИСИ С МАЛЕНЬКИМИ БУКВАМИ, НО ТИПОМ LECTURE")
        print("=" * 80)
        
        for lesson_id, subject, lesson_type, day, pair, parity in results:
            # Проверяем, написано ли в маленьких буквах
            is_lowercase = subject[0].islower() if subject else False
            if is_lowercase and lesson_type == 'lecture':
                print(f"\n❌ ПРОБЛЕМА НАЙДЕНА!")
                print(f"   ID: {lesson_id}")
                print(f"   Предмет: {subject}")
                print(f"   Тип: {lesson_type} (должен быть practice!)")
                print(f"   День: {day}, Пара: {pair}, Четность: {parity}")
                
                # Исправляем
                cur.execute("""
                    UPDATE lessons
                    SET lesson_type = 'practice'
                    WHERE id = %s
                """, (lesson_id,))
                print(f"   ✅ Исправлено на 'practice'")
        
        conn.commit()
    else:
        print("❌ Записей не найдено!")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()
    if 'conn' in locals():
        conn.rollback()

