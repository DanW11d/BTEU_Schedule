"""Проверка записей с NULL lesson_type"""
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
    """Определяет тип занятия по регистру букв: КАПС = лекция, маленькие = практика"""
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
    print("ПРОВЕРКА И ИСПРАВЛЕНИЕ NULL lesson_type")
    print("=" * 80)
    
    # Получаем ID группы S-4
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group_result = cur.fetchone()
    if not group_result:
        print("❌ Группа S-4 не найдена!")
        exit(1)
    group_id = group_result[0]
    
    # Проверяем записи с NULL lesson_type
    cur.execute("""
        SELECT 
            id,
            subject,
            lesson_type
        FROM lessons
        WHERE group_id = %s AND (lesson_type IS NULL OR lesson_type = '')
        ORDER BY subject
    """, (group_id,))
    
    null_results = cur.fetchall()
    
    if null_results:
        print(f"\nНайдено записей с NULL lesson_type: {len(null_results)}\n")
        
        updated_count = 0
        for lesson_id, subject, current_type in null_results:
            correct_type = detect_lesson_type_from_subject(subject)
            
            cur.execute("""
                UPDATE lessons
                SET lesson_type = %s
                WHERE id = %s
            """, (correct_type, lesson_id))
            
            subject_short = subject[:60] + "..." if len(subject) > 60 else subject
            print(f"✅ Обновлено: {subject_short}")
            print(f"   Было: NULL → Стало: {correct_type}")
            updated_count += 1
        
        conn.commit()
        print(f"\n✅ Обновлено {updated_count} записей")
    else:
        print("\n✅ Записей с NULL lesson_type не найдено")
    
    # Проверяем конкретно "Интернет-маркетинг ст."
    print("\n" + "=" * 80)
    print("ПРОВЕРКА: ИНТЕРНЕТ-МАРКЕТИНГ ст.")
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
        WHERE group_id = %s AND subject ILIKE %s
        ORDER BY day_of_week, lesson_number
    """, (group_id, '%интернет%маркетинг%'))
    
    internet_results = cur.fetchall()
    
    if internet_results:
        for lesson_id, subject, lesson_type, day, pair, parity in internet_results:
            subject_short = subject[:70] + "..." if len(subject) > 70 else subject
            day_name = ['ПН', 'ВТ', 'СР', 'ЧТ', 'ПТ', 'СБ'][day - 1] if day <= 6 else '?'
            print(f"\nID: {lesson_id}")
            print(f"  Предмет: {subject_short}")
            print(f"  Тип: {lesson_type or 'NULL'}")
            print(f"  День: {day_name}, Пара: {pair}, Четность: {parity}")
            
            # Проверяем правильность типа
            correct_type = detect_lesson_type_from_subject(subject)
            if lesson_type != correct_type:
                print(f"  ⚠️  НЕПРАВИЛЬНЫЙ ТИП! Должен быть: {correct_type}")
                cur.execute("""
                    UPDATE lessons
                    SET lesson_type = %s
                    WHERE id = %s
                """, (correct_type, lesson_id))
                print(f"  ✅ Исправлено на {correct_type}")
    
    conn.commit()
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()
    if 'conn' in locals():
        conn.rollback()

