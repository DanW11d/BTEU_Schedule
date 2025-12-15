"""ИНВЕРСИЯ ВСЕХ ТИПОВ ЗАНЯТИЙ: КАПС = практика, маленькие = лекция"""
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
    """ИНВЕРТИРОВАННАЯ ЛОГИКА: КАПС (большие буквы) = практика, маленькие = лекция"""
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
    
    # Если больше 50% заглавных (КАПС) → практика
    if uppercase_percentage > 50:
        return 'practice'
    
    # Если больше 20% оставшихся (кроме первой) заглавных → практика
    if total_letters > 1:
        remaining_uppercase = max(0, uppercase_count - 1) if subject[0].isupper() else uppercase_count
        remaining_total = total_letters - 1
        if remaining_total > 0:
            remaining_uppercase_percentage = (remaining_uppercase / remaining_total) * 100
            if remaining_uppercase_percentage > 20:
                return 'practice'
    
    # Иначе (в основном строчные) → лекция
    return 'lecture'

try:
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()
    
    print("=" * 80)
    print("ИНВЕРСИЯ ВСЕХ ТИПОВ ЗАНЯТИЙ: КАПС = ПРАКТИКА, МАЛЕНЬКИЕ = ЛЕКЦИЯ")
    print("=" * 80)
    
    # Получаем ID группы S-4
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group_result = cur.fetchone()
    if not group_result:
        print("❌ Группа S-4 не найдена!")
        exit(1)
    group_id = group_result[0]
    
    # Получаем ВСЕ занятия для группы S-4
    cur.execute("""
        SELECT 
            id,
            subject,
            lesson_type
        FROM lessons
        WHERE group_id = %s
        ORDER BY subject
    """, (group_id,))
    
    results = cur.fetchall()
    
    if not results:
        print("❌ Занятия не найдены!")
        exit(1)
    
    print(f"\nНайдено занятий: {len(results)}\n")
    
    updated_count = 0
    
    for lesson_id, subject, current_type in results:
        # Определяем правильный тип по ИНВЕРТИРОВАННОЙ логике
        correct_type = detect_lesson_type_from_subject(subject)
        
        if current_type != correct_type:
            cur.execute("""
                UPDATE lessons
                SET lesson_type = %s
                WHERE id = %s
            """, (correct_type, lesson_id))
            
            subject_short = subject[:60] + "..." if len(subject) > 60 else subject
            
            # Анализируем регистр для отображения
            uppercase = sum(1 for c in subject if c.isalpha() and c.isupper())
            lowercase = sum(1 for c in subject if c.isalpha() and c.islower())
            total = uppercase + lowercase
            upper_pct = (uppercase / total * 100) if total > 0 else 0
            
            print(f"✅ Обновлено: {subject_short}")
            print(f"   Было: {current_type} → Стало: {correct_type}")
            print(f"   Регистр: {upper_pct:.1f}% заглавных ({uppercase}/{total})")
            updated_count += 1
    
    # Сохраняем изменения
    conn.commit()
    
    print("\n" + "=" * 80)
    print(f"✅ Обновлено занятий: {updated_count}")
    print(f"📋 Всего обработано: {len(results)}")
    print("=" * 80)
    
    # Проверяем результат для вторника
    print("\n" + "=" * 80)
    print("ПРОВЕРКА: ВТОРНИК, НЕЧЕТНАЯ НЕДЕЛЯ")
    print("=" * 80)
    
    cur.execute("""
        SELECT 
            subject,
            lesson_type,
            lesson_number
        FROM lessons
        WHERE group_id = %s AND day_of_week = 2 AND week_parity = 'odd'
        ORDER BY lesson_number
    """, (group_id,))
    
    tuesday_results = cur.fetchall()
    
    if tuesday_results:
        for subject, ltype, pair in tuesday_results:
            subject_short = subject[:70] + "..." if len(subject) > 70 else subject
            print(f"  Пара {pair}: {subject_short}")
            print(f"    Тип: {ltype}")
    else:
        print("❌ Занятий не найдено!")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()
    if 'conn' in locals():
        conn.rollback()

