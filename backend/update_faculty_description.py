"""
Скрипт для обновления описания факультета FKF на "ФКиФ"
"""
import psycopg2
from psycopg2.extras import RealDictCursor
import os
from dotenv import load_dotenv

# Загружаем переменные окружения
load_dotenv()

def get_db_connection():
    """Создать подключение к базе данных"""
    return psycopg2.connect(
        host=os.getenv('DB_HOST', 'localhost'),
        port=os.getenv('DB_PORT', '5432'),
        database=os.getenv('DB_NAME', 'bteu_schedule'),
        user=os.getenv('DB_USER', 'postgres'),
        password=os.getenv('DB_PASSWORD', 'postgres')
    )

def main():
    conn = None
    try:
        conn = get_db_connection()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        print("=" * 60)
        print("Обновление описания факультета FKF")
        print("=" * 60)
        
        # 1. Проверить текущее состояние факультета FKF
        cur.execute("""
            SELECT id, code, name_ru, description, is_active
            FROM faculties 
            WHERE code = 'FKF'
        """)
        faculty = cur.fetchone()
        
        if not faculty:
            print("ОШИБКА: Факультет FKF не найден!")
            return
        
        print(f"\nТекущее состояние факультета FKF:")
        print(f"  ID: {faculty['id']}")
        print(f"  Код: {faculty['code']}")
        print(f"  Название: {faculty['name_ru']}")
        print(f"  Описание: {faculty['description']}")
        
        # 2. Проверить количество групп, связанных с FKF
        cur.execute("SELECT COUNT(*) as count FROM groups WHERE faculty_id = %s", (faculty['id'],))
        groups_count = cur.fetchone()['count']
        print(f"\nГрупп, связанных с факультетом FKF: {groups_count}")
        
        # 3. Обновить описание на "ФКиФ"
        print("\n" + "=" * 60)
        print("Обновление описания на 'ФКиФ'...")
        print("=" * 60)
        
        cur.execute("""
            UPDATE faculties 
            SET description = 'ФКиФ'
            WHERE code = 'FKF'
        """)
        
        updated = cur.rowcount
        if updated > 0:
            print("Описание успешно обновлено!")
        else:
            print("Не удалось обновить описание")
        
        # 4. Показать обновленный результат
        cur.execute("""
            SELECT id, code, name_ru, description, is_active
            FROM faculties 
            WHERE code = 'FKF'
        """)
        updated_faculty = cur.fetchone()
        
        print("\n" + "=" * 60)
        print("Обновленное состояние факультета FKF:")
        print("=" * 60)
        print(f"  ID: {updated_faculty['id']}")
        print(f"  Код: {updated_faculty['code']}")
        print(f"  Название: {updated_faculty['name_ru']}")
        print(f"  Описание: {updated_faculty['description']}")
        print(f"  Активен: {updated_faculty['is_active']}")
        
        # 5. Показать все группы, связанные с FKF
        if groups_count > 0:
            cur.execute("""
                SELECT id, code, name, course, education_form
                FROM groups 
                WHERE faculty_id = %s
                ORDER BY course, code
                LIMIT 10
            """, (updated_faculty['id'],))
            groups = cur.fetchall()
            
            print("\n" + "=" * 60)
            print(f"Группы факультета FKF (показано {min(10, groups_count)} из {groups_count}):")
            print("=" * 60)
            for g in groups:
                print(f"  - {g['code']}: {g['name']} ({g['course']} курс, {g['education_form']})")
        
        # Подтвердить изменения
        conn.commit()
        print("\n" + "=" * 60)
        print("Изменения успешно применены!")
        print("=" * 60)
        
    except Exception as e:
        if conn:
            conn.rollback()
        print(f"\nОШИБКА: {e}")
        raise
    finally:
        if conn:
            cur.close()
            conn.close()

if __name__ == "__main__":
    main()

