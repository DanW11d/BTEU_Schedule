"""Исправление ВСЕХ типов занятий на основе регистра букв в названии"""
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
    
    # Подсчитываем количество заглавных и строчных букв
    uppercase_count = 0
    lowercase_count = 0
    total_letters = 0
    
    for char in subject:
        if char.isalpha():
            total_letters += 1
            if char.isupper():
                uppercase_count += 1
            elif char.islower():
                lowercase_count += 1
    
    # Если нет букв, возвращаем лекцию по умолчанию
    if total_letters == 0:
        return 'lecture'
    
    # Вычисляем процент заглавных букв
    uppercase_percentage = (uppercase_count / total_letters) * 100
    
    # Если больше 50% букв заглавные → лекция
    if uppercase_percentage > 50:
        return 'lecture'
    
    # Если больше 20% оставшихся букв (кроме первой) заглавные → лекция
    if total_letters > 1:
        remaining_uppercase = max(0, uppercase_count - 1) if subject[0].isupper() else uppercase_count
        remaining_total = total_letters - 1
        if remaining_total > 0:
            remaining_uppercase_percentage = (remaining_uppercase / remaining_total) * 100
            if remaining_uppercase_percentage > 20:
                return 'lecture'
    
    # Иначе → практика
    return 'practice'

try:
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()
    
    print("=" * 80)
    print("ИСПРАВЛЕНИЕ ВСЕХ ТИПОВ ЗАНЯТИЙ НА ОСНОВЕ РЕГИСТРА БУКВ")
    print("=" * 80)
    
    # Получаем ID группы S-4
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group_result = cur.fetchone()
    if not group_result:
        print("❌ Группа S-4 не найдена!")
        exit(1)
    group_id = group_result[0]
    
    # Получаем все занятия для группы S-4
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
    unchanged_count = 0
    
    for lesson_id, subject, current_type in results:
        # Определяем правильный тип на основе регистра
        correct_type = detect_lesson_type_from_subject(subject)
        
        if current_type != correct_type:
            # Обновляем тип занятия
            cur.execute("""
                UPDATE lessons
                SET lesson_type = %s
                WHERE id = %s
            """, (correct_type, lesson_id))
            
            subject_short = subject[:60] + "..." if len(subject) > 60 else subject
            print(f"✅ Обновлено: {subject_short}")
            print(f"   Было: {current_type} → Стало: {correct_type}")
            updated_count += 1
        else:
            unchanged_count += 1
    
    # Сохраняем изменения
    conn.commit()
    
    print("\n" + "=" * 80)
    print(f"✅ Обновлено занятий: {updated_count}")
    print(f"📋 Без изменений: {unchanged_count}")
    print("=" * 80)
    
    # Проверяем результат для понедельника, пара 1
    print("\n" + "=" * 80)
    print("ПРОВЕРКА: ПОНЕДЕЛЬНИК, ПАРА 1")
    print("=" * 80)
    
    cur.execute("""
        SELECT 
            subject,
            lesson_type,
            week_parity
        FROM lessons
        WHERE group_id = %s AND day_of_week = 1 AND lesson_number = 1
        ORDER BY week_parity
    """, (group_id,))
    
    monday_results = cur.fetchall()
    
    if monday_results:
        for subject, ltype, parity in monday_results:
            subject_short = subject[:70] + "..." if len(subject) > 70 else subject
            print(f"  {subject_short}")
            print(f"    Тип: {ltype}, Четность: {parity}")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()
    if 'conn' in locals():
        conn.rollback()

