"""
Скрипт для удаления кафедр с id от 1 до 10 включительно
"""
import psycopg2
import os
from dotenv import load_dotenv

load_dotenv()

# Настройки подключения к БД
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': int(os.getenv('DB_PORT', '5432')),
    'database': os.getenv('DB_NAME', 'postgres'),
    'user': os.getenv('DB_USER', 'postgres'),
    'password': os.getenv('DB_PASSWORD', '7631')
}

def delete_departments_1_to_10():
    """Удаляет кафедры с id от 1 до 10 включительно"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()
        
        print("Удаление кафедр с id от 1 до 10...")
        
        # Проверяем, какие кафедры будут удалены
        cur.execute("""
            SELECT id, code, name_ru 
            FROM departments 
            WHERE id >= 1 AND id <= 10
            ORDER BY id
        """)
        
        departments_to_delete = cur.fetchall()
        
        if not departments_to_delete:
            print("Кафедры с id от 1 до 10 не найдены.")
            cur.close()
            conn.close()
            return
        
        print(f"\nБудут удалены следующие кафедры ({len(departments_to_delete)} шт.):")
        for dept_id, code, name in departments_to_delete:
            print(f"  - ID: {dept_id}, Код: {code}, Название: {name}")
        
        # Удаляем кафедры
        cur.execute("""
            DELETE FROM departments
            WHERE id >= 1 AND id <= 10
        """)
        
        deleted_count = cur.rowcount
        
        # Сохраняем изменения
        conn.commit()
        print(f"\n[OK] Удалено кафедр: {deleted_count}")
        
        # Проверяем результаты
        cur.execute("""
            SELECT 
                COUNT(*) as total_departments,
                MIN(id) as min_id,
                MAX(id) as max_id
            FROM departments
            WHERE is_active = TRUE
        """)
        
        result = cur.fetchone()
        total, min_id, max_id = result
        
        print(f"\nТекущее состояние таблицы departments:")
        print(f"  Всего активных кафедр: {total}")
        print(f"  Минимальный ID: {min_id}")
        print(f"  Максимальный ID: {max_id}")
        
        cur.close()
        conn.close()
        
    except Exception as e:
        print(f"[ERROR] Ошибка: {e}")
        if conn:
            conn.rollback()
            conn.close()
        raise

if __name__ == '__main__':
    delete_departments_1_to_10()

