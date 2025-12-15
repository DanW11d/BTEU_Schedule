"""Принудительное исправление ВСЕХ типов занятий на основе регистра"""
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
    
    # Если больше 50% заглавных → лекция
    if uppercase_percentage > 50:
        return 'lecture'
    
    # Если больше 20% оставшихся (кроме первой) заглавных → лекция
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
    print("ПРИНУДИТЕЛЬНОЕ ИСПРАВЛЕНИЕ ВСЕХ ТИПОВ ЗАНЯТИЙ")
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
        # Определяем правильный тип на основе регистра
        correct_type = detect_lesson_type_from_subject(subject)
        
        # ВСЕГДА обновляем, даже если тип уже правильный (для пересчета)
        cur.execute("""
            UPDATE lessons
            SET lesson_type = %s
            WHERE id = %s
        """, (correct_type, lesson_id))
        
        if current_type != correct_type:
            subject_short = subject[:60] + "..." if len(subject) > 60 else subject
            print(f"✅ Обновлено: {subject_short}")
            print(f"   Было: {current_type} → Стало: {correct_type}")
            updated_count += 1
    
    # Сохраняем изменения
    conn.commit()
    
    print("\n" + "=" * 80)
    print(f"✅ Всего обновлено занятий: {len(results)}")
    print(f"✅ Изменено типов: {updated_count}")
    print("=" * 80)
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()
    if 'conn' in locals():
        conn.rollback()

