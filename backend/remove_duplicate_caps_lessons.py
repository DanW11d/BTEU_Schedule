"""Удаление дубликатов занятий с КАПС для предметов, которые должны быть в маленьких буквах"""
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

def is_mostly_uppercase(text: str) -> bool:
    """Проверяет, написано ли название в основном заглавными буквами"""
    if not text:
        return False
    
    uppercase_count = 0
    total_letters = 0
    
    for char in text:
        if char.isalpha():
            total_letters += 1
            if char.isupper():
                uppercase_count += 1
    
    if total_letters == 0:
        return False
    
    uppercase_percentage = (uppercase_count / total_letters) * 100
    return uppercase_percentage > 50

def normalize_subject(subject: str) -> str:
    """Нормализует название предмета для сравнения (убирает регистр)"""
    return subject.lower().strip()

try:
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()
    
    print("=" * 80)
    print("УДАЛЕНИЕ ДУБЛИКАТОВ С КАПС")
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
            lesson_type,
            day_of_week,
            lesson_number,
            week_parity
        FROM lessons
        WHERE group_id = %s
        ORDER BY subject, day_of_week, lesson_number
    """, (group_id,))
    
    results = cur.fetchall()
    
    if not results:
        print("❌ Занятия не найдены!")
        exit(1)
    
    print(f"\nНайдено занятий: {len(results)}\n")
    
    # Группируем по нормализованному названию
    subject_groups = {}
    for lesson_id, subject, lesson_type, day_of_week, lesson_number, week_parity in results:
        normalized = normalize_subject(subject)
        if normalized not in subject_groups:
            subject_groups[normalized] = []
        subject_groups[normalized].append({
            'id': lesson_id,
            'subject': subject,
            'type': lesson_type,
            'day': day_of_week,
            'pair': lesson_number,
            'parity': week_parity
        })
    
    # Ищем дубликаты
    duplicates_to_remove = []
    
    for normalized_subject, lessons in subject_groups.items():
        if len(lessons) > 1:
            # Есть несколько записей с одинаковым названием (без учета регистра)
            # Находим записи с КАПС и маленькими буквами
            caps_lessons = [l for l in lessons if is_mostly_uppercase(l['subject'])]
            lowercase_lessons = [l for l in lessons if not is_mostly_uppercase(l['subject'])]
            
            if caps_lessons and lowercase_lessons:
                # Есть и КАПС и маленькие - удаляем КАПС
                print(f"\n📚 Предмет: {normalized_subject}")
                print(f"   КАПС записей: {len(caps_lessons)}")
                print(f"   Маленькие записи: {len(lowercase_lessons)}")
                
                for caps_lesson in caps_lessons:
                    # Проверяем, есть ли соответствующая запись с маленькими буквами
                    matching_lowercase = None
                    for lower_lesson in lowercase_lessons:
                        if (lower_lesson['day'] == caps_lesson['day'] and 
                            lower_lesson['pair'] == caps_lesson['pair'] and
                            lower_lesson['parity'] == caps_lesson['parity']):
                            matching_lowercase = lower_lesson
                            break
                    
                    if matching_lowercase:
                        print(f"   ✅ Найден дубликат: ID {caps_lesson['id']} (КАПС) → удаляем")
                        print(f"      Оставляем: ID {matching_lowercase['id']} (маленькие)")
                        duplicates_to_remove.append(caps_lesson['id'])
                    else:
                        print(f"   ⚠️  КАПС запись ID {caps_lesson['id']} без пары - оставляем")
    
    # Удаляем дубликаты
    if duplicates_to_remove:
        print(f"\n" + "=" * 80)
        print(f"Удаление {len(duplicates_to_remove)} дубликатов...")
        print("=" * 80)
        
        for lesson_id in duplicates_to_remove:
            cur.execute("DELETE FROM lessons WHERE id = %s", (lesson_id,))
            print(f"✅ Удалено занятие ID {lesson_id}")
        
        conn.commit()
        print(f"\n✅ Успешно удалено {len(duplicates_to_remove)} дубликатов")
    else:
        print("\n✅ Дубликатов не найдено")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()
    if 'conn' in locals():
        conn.rollback()

