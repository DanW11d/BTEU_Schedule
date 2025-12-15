"""ИНВЕРСИЯ ТИПОВ ЗАНЯТИЙ ДЛЯ ВСЕХ ГРУПП: КАПС = практика, маленькие = лекция"""
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
    print("ИНВЕРСИЯ ТИПОВ ЗАНЯТИЙ ДЛЯ ВСЕХ ГРУПП")
    print("КАПС (большие буквы) = ПРАКТИКА, маленькие = ЛЕКЦИЯ")
    print("=" * 80)
    
    # Получаем все активные группы
    cur.execute("SELECT id, code FROM groups WHERE is_active = TRUE ORDER BY code")
    groups = cur.fetchall()
    
    if not groups:
        print("❌ Группы не найдены!")
        exit(1)
    
    print(f"\nНайдено групп: {len(groups)}\n")
    
    total_updated = 0
    total_lessons = 0
    
    for group_id, group_code in groups:
        # Получаем все занятия для группы
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
            continue
        
        group_updated = 0
        for lesson_id, subject, current_type in results:
            # Определяем правильный тип по ИНВЕРТИРОВАННОЙ логике
            correct_type = detect_lesson_type_from_subject(subject)
            
            if current_type != correct_type:
                cur.execute("""
                    UPDATE lessons
                    SET lesson_type = %s
                    WHERE id = %s
                """, (correct_type, lesson_id))
                group_updated += 1
        
        if group_updated > 0:
            print(f"✅ Группа {group_code}: обновлено {group_updated} из {len(results)} занятий")
            total_updated += group_updated
        
        total_lessons += len(results)
    
    # Сохраняем изменения
    conn.commit()
    
    print("\n" + "=" * 80)
    print(f"✅ Всего обновлено занятий: {total_updated}")
    print(f"📋 Всего обработано занятий: {total_lessons}")
    print(f"📋 Всего обработано групп: {len(groups)}")
    print("=" * 80)
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()
    if 'conn' in locals():
        conn.rollback()

