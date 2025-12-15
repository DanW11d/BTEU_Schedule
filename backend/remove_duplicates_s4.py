"""Удаление дубликатов для группы S-4"""
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
    
    print("=" * 60)
    print("УДАЛЕНИЕ ДУБЛИКАТОВ ДЛЯ ГРУППЫ S-4")
    print("=" * 60)
    
    # Получаем ID группы
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group = cur.fetchone()
    
    if not group:
        print("\n❌ Группа S-4 не найдена!")
        exit(1)
    
    group_id = group[0]
    
    # Находим дубликаты: одинаковые предмет, преподаватель, аудитория, день, неделя
    # Оставляем только одно занятие с минимальным ID
    print("\nПоиск дубликатов...")
    
    cur.execute("""
        SELECT 
            l1.id,
            l1.lesson_number,
            l1.day_of_week,
            l1.subject,
            l1.week_parity,
            COUNT(*) as duplicate_count
        FROM lessons l1
        INNER JOIN lessons l2 ON (
            l1.group_id = l2.group_id
            AND l1.day_of_week = l2.day_of_week
            AND l1.week_parity = l2.week_parity
            AND LOWER(TRIM(l1.subject)) = LOWER(TRIM(l2.subject))
            AND COALESCE(l1.teacher, '') = COALESCE(l2.teacher, '')
            AND COALESCE(l1.classroom, '') = COALESCE(l2.classroom, '')
            AND l1.id != l2.id
        )
        WHERE l1.group_id = %s
        GROUP BY l1.id, l1.lesson_number, l1.day_of_week, l1.subject, l1.week_parity
        HAVING COUNT(*) > 0
        ORDER BY l1.day_of_week, l1.lesson_number, l1.id
    """, (group_id,))
    
    duplicates = cur.fetchall()
    
    if not duplicates:
        print("✅ Дубликатов не найдено")
    else:
        print(f"\n⚠ Найдено потенциальных дубликатов: {len(duplicates)}")
        
        # Для каждого дубликата находим все записи с таким же предметом
        to_delete = []
        processed_subjects = set()
        
        for dup in duplicates:
            dup_id, lesson_num, day, subject, week_parity, count = dup
            subject_key = (day, week_parity, subject.lower().strip())
            
            if subject_key in processed_subjects:
                continue
            
            processed_subjects.add(subject_key)
            
            # Находим все записи с таким же предметом
            cur.execute("""
                SELECT id, lesson_number
                FROM lessons
                WHERE group_id = %s
                    AND day_of_week = %s
                    AND week_parity = %s
                    AND LOWER(TRIM(subject)) = LOWER(TRIM(%s))
                ORDER BY id
            """, (group_id, day, week_parity, subject))
            
            same_subjects = cur.fetchall()
            
            if len(same_subjects) > 1:
                print(f"\nДень {day}, Неделя {week_parity}, Предмет: {subject[:50]}...")
                print(f"  Найдено записей: {len(same_subjects)}")
                
                # Оставляем только одну запись (с минимальным ID или правильным номером пары)
                # Удаляем остальные
                keep_id = same_subjects[0][0]  # Оставляем первую запись
                delete_ids = [s[0] for s in same_subjects[1:]]
                
                print(f"  Оставляем ID: {keep_id} (Пара {same_subjects[0][1]})")
                print(f"  Удаляем ID: {delete_ids}")
                
                to_delete.extend(delete_ids)
        
        if to_delete:
            print(f"\n\nУдаление {len(to_delete)} дубликатов...")
            for delete_id in to_delete:
                cur.execute("DELETE FROM lessons WHERE id = %s", (delete_id,))
            
            conn.commit()
            print(f"✅ Удалено {len(to_delete)} дубликатов")
        else:
            print("\n✅ Дубликатов для удаления не найдено")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

