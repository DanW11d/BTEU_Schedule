"""Исправление типов занятий в базе данных на основе регистра букв"""
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
    """Определяет тип занятия по регистру букв в названии предмета"""
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
    
    if total_letters == 0:
        return 'lecture'
    
    uppercase_percentage = (uppercase_count / total_letters) * 100
    
    # Проверяем оставшиеся буквы (без первой)
    if total_letters > 1:
        remaining_uppercase = max(0, uppercase_count - 1) if subject[0].isupper() else uppercase_count
        remaining_total = total_letters - 1
        if remaining_total > 0:
            remaining_uppercase_percentage = (remaining_uppercase / remaining_total) * 100
            if remaining_uppercase_percentage > 20:
                return 'lecture'
    
    if uppercase_percentage > 50:
        return 'lecture'
    
    return 'practice'

try:
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()
    
    print("=" * 80)
    print("ИСПРАВЛЕНИЕ ТИПОВ ЗАНЯТИЙ В БАЗЕ ДАННЫХ")
    print("=" * 80)
    
    # Получаем ID группы
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group_result = cur.fetchone()
    if not group_result:
        print("❌ Группа S-4 не найдена!")
        exit(1)
    group_id = group_result[0]
    
    # Получаем все занятия
    cur.execute("""
        SELECT id, subject, lesson_type
        FROM lessons
        WHERE group_id = %s
        ORDER BY id
    """, (group_id,))
    
    lessons = cur.fetchall()
    
    print(f"\nВсего занятий: {len(lessons)}\n")
    
    updated_count = 0
    fixed_subjects = []
    
    for lesson_id, subject, current_type in lessons:
        # Определяем правильный тип
        correct_type = detect_lesson_type_from_subject(subject)
        
        if current_type != correct_type:
            # Обновляем тип
            cur.execute("""
                UPDATE lessons
                SET lesson_type = %s
                WHERE id = %s
            """, (correct_type, lesson_id))
            
            updated_count += 1
            type_display_old = {'lecture': 'ЛЕКЦИЯ', 'practice': 'Практика'}.get(current_type, current_type)
            type_display_new = {'lecture': 'ЛЕКЦИЯ', 'practice': 'Практика'}.get(correct_type, correct_type)
            fixed_subjects.append((subject[:50], type_display_old, type_display_new))
    
    conn.commit()
    
    print(f"✅ Обновлено занятий: {updated_count}\n")
    
    if fixed_subjects:
        print("Исправленные предметы:")
        for subject, old_type, new_type in fixed_subjects[:10]:  # Показываем первые 10
            print(f"  {subject:50s} | {old_type:10s} → {new_type}")
        if len(fixed_subjects) > 10:
            print(f"  ... и еще {len(fixed_subjects) - 10} предметов")
    
    # Проверяем результат
    print("\n" + "=" * 80)
    print("ПРОВЕРКА РЕЗУЛЬТАТА")
    print("=" * 80)
    
    cur.execute("""
        SELECT lesson_type, COUNT(*) as count
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

