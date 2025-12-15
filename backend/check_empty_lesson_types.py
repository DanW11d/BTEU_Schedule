"""Проверка пустых строк в lesson_type"""
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

def detect_lesson_type_from_subject(subject: str) -> str:
    """КАПС = лекция, маленькие = практика"""
    if not subject:
        return 'lecture'
    
    uppercase_count = 0
    total_letters = 0
    
    for char in subject:
        if char.isalpha():
            total_letters += 1
            if char.isupper():
                uppercase_count += 1
    
    if total_letters == 0:
        return 'lecture'
    
    uppercase_percentage = (uppercase_count / total_letters) * 100
    
    if uppercase_percentage > 50:
        return 'lecture'
    
    if total_letters > 1:
        remaining_uppercase = max(0, uppercase_count - 1) if subject[0].isupper() else uppercase_count
        remaining_total = total_letters - 1
        if remaining_total > 0:
            remaining_uppercase_percentage = (remaining_uppercase / remaining_total) * 100
            if remaining_uppercase_percentage > 20:
                return 'lecture'
    
    return 'practice'

try:
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()
    
    print("=" * 80)
    print("ПРОВЕРКА ПУСТЫХ СТРОК И ИСПРАВЛЕНИЕ lesson_type")
    print("=" * 80)
    
    # Получаем ID группы S-4
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group_result = cur.fetchone()
    if not group_result:
        print("❌ Группа S-4 не найдена!")
        exit(1)
    group_id = group_result[0]
    
    # Проверяем пустые строки и NULL
    cur.execute("""
        SELECT 
            id,
            subject,
            lesson_type,
            CASE 
                WHEN lesson_type IS NULL THEN 'NULL'
                WHEN lesson_type = '' THEN 'EMPTY'
                ELSE lesson_type
            END as type_status
        FROM lessons
        WHERE group_id = %s
        ORDER BY subject
    """, (group_id,))
    
    results = cur.fetchall()
    
    if results:
        print(f"\nНайдено занятий: {len(results)}\n")
        
        updated_count = 0
        for lesson_id, subject, current_type, type_status in results:
            # Если NULL или пустая строка, исправляем
            if current_type is None or current_type == '':
                correct_type = detect_lesson_type_from_subject(subject)
                
                cur.execute("""
                    UPDATE lessons
                    SET lesson_type = %s
                    WHERE id = %s
                """, (correct_type, lesson_id))
                
                subject_short = subject[:60] + "..." if len(subject) > 60 else subject
                print(f"✅ Исправлено: {subject_short}")
                print(f"   Было: {type_status} → Стало: {correct_type}")
                updated_count += 1
            else:
                # Проверяем правильность типа
                correct_type = detect_lesson_type_from_subject(subject)
                if current_type != correct_type:
                    cur.execute("""
                        UPDATE lessons
                        SET lesson_type = %s
                        WHERE id = %s
                    """, (correct_type, lesson_id))
                    
                    subject_short = subject[:60] + "..." if len(subject) > 60 else subject
                    print(f"✅ Исправлено: {subject_short}")
                    print(f"   Было: {current_type} → Стало: {correct_type}")
                    updated_count += 1
        
        conn.commit()
        
        print("\n" + "=" * 80)
        print(f"✅ Обновлено занятий: {updated_count}")
        print("=" * 80)
    else:
        print("❌ Занятия не найдены!")
    
    # Проверяем конкретно "Интернет-маркетинг ст." для понедельника, пара 1
    print("\n" + "=" * 80)
    print("ФИНАЛЬНАЯ ПРОВЕРКА: ИНТЕРНЕТ-МАРКЕТИНГ ст., ПН, ПАРА 1")
    print("=" * 80)
    
    cur.execute("""
        SELECT 
            id,
            subject,
            lesson_type,
            day_of_week,
            lesson_number,
            week_parity
        FROM lessons
        WHERE group_id = %s 
          AND subject ILIKE %s
          AND day_of_week = 1
          AND lesson_number = 1
          AND week_parity = 'odd'
    """, (group_id, '%интернет%маркетинг%'))
    
    final_check = cur.fetchall()
    
    if final_check:
        for lesson_id, subject, lesson_type, day, pair, parity in final_check:
            print(f"\nID: {lesson_id}")
            print(f"  Предмет: {subject}")
            print(f"  Тип в БД: {lesson_type}")
            print(f"  Правильный тип (по регистру): {detect_lesson_type_from_subject(subject)}")
            
            if lesson_type != detect_lesson_type_from_subject(subject):
                print(f"  ⚠️  ТИП НЕПРАВИЛЬНЫЙ! Исправляем...")
                correct_type = detect_lesson_type_from_subject(subject)
                cur.execute("""
                    UPDATE lessons
                    SET lesson_type = %s
                    WHERE id = %s
                """, (correct_type, lesson_id))
                conn.commit()
                print(f"  ✅ Исправлено на {correct_type}")
            else:
                print(f"  ✅ Тип правильный!")
    else:
        print("❌ Запись не найдена!")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()
    if 'conn' in locals():
        conn.rollback()

