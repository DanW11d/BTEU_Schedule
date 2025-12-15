"""Удаление занятий четной недели, если они не должны быть там"""
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
    print("УДАЛЕНИЕ ЗАНЯТИЙ ЧЕТНОЙ НЕДЕЛИ (если нет разделения)")
    print("=" * 60)
    
    # Получаем ID группы
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group = cur.fetchone()
    group_id = group[0]
    
    # Проверяем, есть ли одинаковые предметы в разные недели
    # Если есть - это означает, что они должны чередоваться
    # Если нет - удаляем четную неделю
    print("\n1. Проверка чередования занятий...")
    
    cur.execute("""
        SELECT 
            day_of_week,
            lesson_number,
            subject,
            COUNT(DISTINCT week_parity) as parity_count
        FROM lessons
        WHERE group_id = %s
        GROUP BY day_of_week, lesson_number, subject
        HAVING COUNT(DISTINCT week_parity) > 1
    """, (group_id,))
    
    alternating_lessons = cur.fetchall()
    
    if alternating_lessons:
        print(f"   ⚠ Найдено {len(alternating_lessons)} предметов, которые чередуются по неделям")
        print("   Эти занятия должны остаться в обеих неделях")
    else:
        print("   ✅ Чередующихся занятий не найдено")
        print("   Все занятия должны быть только в нечетной неделе")
    
    # Находим занятия четной недели, которые не чередуются
    print("\n2. Поиск занятий четной недели для удаления...")
    
    if alternating_lessons:
        # Если есть чередующиеся занятия, удаляем только те, которые не чередуются
        # Создаем список чередующихся комбинаций
        alternating_keys = set()
        for day, lesson_num, subject, _ in alternating_lessons:
            alternating_keys.add((day, lesson_num, subject))
        
        cur.execute("""
            SELECT id, day_of_week, lesson_number, subject
            FROM lessons
            WHERE group_id = %s AND week_parity = 'even'
        """, (group_id,))
        
        even_lessons = cur.fetchall()
        to_delete = []
        
        for lesson_id, day, lesson_num, subject in even_lessons:
            key = (day, lesson_num, subject)
            if key not in alternating_keys:
                to_delete.append(lesson_id)
        
        print(f"   Найдено {len(to_delete)} занятий четной недели, которые не чередуются")
    else:
        # Если нет чередующихся занятий, удаляем все занятия четной недели
        cur.execute("""
            SELECT COUNT(*) FROM lessons
            WHERE group_id = %s AND week_parity = 'even'
        """, (group_id,))
        
        even_count = cur.fetchone()[0]
        print(f"   Найдено {even_count} занятий четной недели")
        
        if even_count > 0:
            cur.execute("""
                DELETE FROM lessons
                WHERE group_id = %s AND week_parity = 'even'
            """, (group_id,))
            deleted_count = cur.rowcount
            conn.commit()
            print(f"   ✅ Удалено {deleted_count} занятий четной недели")
        else:
            print("   ✅ Занятий четной недели нет")
    
    cur.close()
    conn.close()
    
    print("\n" + "=" * 60)
    print("ГОТОВО!")
    print("=" * 60)
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

