"""Исправление четности для дней с одним разделителем"""
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
    print("ИСПРАВЛЕНИЕ ЧЕТНОСТИ ДЛЯ ДНЕЙ С ОДНИМ РАЗДЕЛИТЕЛЕМ")
    print("=" * 60)
    
    # Получаем ID группы
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group = cur.fetchone()
    group_id = group[0]
    
    # Находим дни, где есть занятия и четной, и нечетной недели
    # Если в дне есть занятия обеих недель, но нет чередования (одинаковых предметов),
    # значит это ошибка парсинга - нужно оставить только нечетную
    print("\n1. Поиск дней с занятиями обеих недель...")
    
    cur.execute("""
        SELECT 
            day_of_week,
            COUNT(DISTINCT week_parity) as parity_count,
            COUNT(*) FILTER (WHERE week_parity = 'odd') as odd_count,
            COUNT(*) FILTER (WHERE week_parity = 'even') as even_count
        FROM lessons
        WHERE group_id = %s
        GROUP BY day_of_week
        HAVING COUNT(DISTINCT week_parity) > 1
        ORDER BY day_of_week
    """, (group_id,))
    
    days_with_both = cur.fetchall()
    
    if not days_with_both:
        print("   ✅ Все дни имеют занятия только одной недели")
    else:
        print(f"   Найдено дней с обеими неделями: {len(days_with_both)}\n")
        
        for day, parity_count, odd_count, even_count in days_with_both:
            day_name = ['ПН', 'ВТ', 'СР', 'ЧТ', 'ПТ', 'СБ'][day - 1]
            print(f"   День {day} ({day_name}): Нечетная: {odd_count}, Четная: {even_count}")
            
            # Проверяем, есть ли чередование (одинаковые предметы в разные недели)
            cur.execute("""
                SELECT COUNT(*)
                FROM (
                    SELECT day_of_week, lesson_number, subject
                    FROM lessons
                    WHERE group_id = %s AND day_of_week = %s
                    GROUP BY day_of_week, lesson_number, subject
                    HAVING COUNT(DISTINCT week_parity) > 1
                ) as alternating
            """, (group_id, day))
            
            alternating_count = cur.fetchone()[0]
            
            if alternating_count == 0:
                print(f"      ⚠ Нет чередования - все занятия должны быть нечетной недели")
                # Удаляем занятия четной недели для этого дня
                cur.execute("""
                    DELETE FROM lessons
                    WHERE group_id = %s AND day_of_week = %s AND week_parity = 'even'
                """, (group_id, day))
                deleted = cur.rowcount
                print(f"      ✅ Удалено {deleted} занятий четной недели")
            else:
                print(f"      ✅ Есть чередование ({alternating_count} предметов) - оставляем обе недели")
    
    conn.commit()
    cur.close()
    conn.close()
    
    print("\n" + "=" * 60)
    print("ГОТОВО!")
    print("=" * 60)
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

