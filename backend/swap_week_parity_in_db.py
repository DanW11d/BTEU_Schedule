"""Поменять местами четную и нечетную неделю в базе данных"""
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
    
    print("=" * 80)
    print("ИНВЕРСИЯ ЧЕТНОСТИ НЕДЕЛИ: ODD ↔ EVEN")
    print("=" * 80)
    
    # Получаем статистику перед изменением
    cur.execute("""
        SELECT 
            week_parity,
            COUNT(*) as count
        FROM lessons
        WHERE week_parity IN ('odd', 'even')
        GROUP BY week_parity
    """)
    
    stats_before = cur.fetchall()
    print("\nСтатистика ДО изменения:")
    for parity, count in stats_before:
        print(f"  {parity}: {count} занятий")
    
    # Меняем местами odd и even используя CASE
    print("\n" + "=" * 80)
    print("Обновление записей...")
    print("=" * 80)
    
    # Обновляем все записи за один раз используя CASE
    cur.execute("""
        UPDATE lessons
        SET week_parity = CASE 
            WHEN week_parity = 'odd' THEN 'even'
            WHEN week_parity = 'even' THEN 'odd'
            ELSE week_parity
        END
        WHERE week_parity IN ('odd', 'even')
    """)
    total_updated = cur.rowcount
    print(f"✅ Обновлено {total_updated} записей (odd → even, even → odd)")
    
    # Сохраняем изменения
    conn.commit()
    
    # Получаем статистику после изменения
    cur.execute("""
        SELECT 
            week_parity,
            COUNT(*) as count
        FROM lessons
        WHERE week_parity IN ('odd', 'even')
        GROUP BY week_parity
    """)
    
    stats_after = cur.fetchall()
    print("\n" + "=" * 80)
    print("Статистика ПОСЛЕ изменения:")
    for parity, count in stats_after:
        print(f"  {parity}: {count} занятий")
    
    print("\n" + "=" * 80)
    print(f"✅ Всего обновлено: {total_updated} записей")
    print("=" * 80)
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()
    if 'conn' in locals():
        conn.rollback()

