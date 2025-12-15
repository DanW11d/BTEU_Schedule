"""
Скрипт для удаления дубликата факультета FKIF (id=1)
Факультет FKF (id=4) является дубликатом с тем же названием
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
        print("Удаление дубликата факультета FKIF (id=1)")
        print("=" * 60)
        
        # 1. Проверить зависимости в groups
        cur.execute("SELECT COUNT(*) as count FROM groups WHERE faculty_id = 1")
        groups_count = cur.fetchone()['count']
        print(f"\nГрупп, связанных с факультетом id=1: {groups_count}")
        
        if groups_count > 0:
            cur.execute("""
                SELECT id, code, name, course, education_form 
                FROM groups 
                WHERE faculty_id = 1 
                LIMIT 5
            """)
            groups = cur.fetchall()
            print("Примеры групп:")
            for g in groups:
                print(f"  - {g['code']}: {g['name']} ({g['course']} курс, {g['education_form']})")
        
        # 2. Проверить зависимости в departments
        cur.execute("SELECT COUNT(*) as count FROM departments WHERE faculty_id = 1")
        dept_count = cur.fetchone()['count']
        print(f"\nКафедр, связанных с факультетом id=1: {dept_count}")
        
        if dept_count > 0:
            cur.execute("""
                SELECT id, code, name_ru 
                FROM departments 
                WHERE faculty_id = 1
            """)
            departments = cur.fetchall()
            print("Кафедры:")
            for d in departments:
                print(f"  - {d['code']}: {d['name_ru']}")
        
        # 3. Переназначить зависимости на факультет FKF (id=4)
        if groups_count > 0 or dept_count > 0:
            print("\n" + "=" * 60)
            print("Переназначение зависимостей на факультет FKF (id=4)...")
            print("=" * 60)
            
            # Обновить группы
            if groups_count > 0:
                cur.execute("UPDATE groups SET faculty_id = 4 WHERE faculty_id = 1")
                updated_groups = cur.rowcount
                print(f"Обновлено групп: {updated_groups}")
            
            # Обновить кафедры
            if dept_count > 0:
                cur.execute("UPDATE departments SET faculty_id = 4 WHERE faculty_id = 1")
                updated_depts = cur.rowcount
                print(f"Обновлено кафедр: {updated_depts}")
        
        # 4. Удалить дубликат факультета
        print("\n" + "=" * 60)
        print("Удаление факультета FKIF (id=1)...")
        print("=" * 60)
        
        cur.execute("DELETE FROM faculties WHERE id = 1")
        deleted = cur.rowcount
        
        if deleted > 0:
            print(f"Факультет FKIF (id=1) успешно удален")
        else:
            print("Факультет с id=1 не найден")
        
        # 5. Показать оставшиеся факультеты
        cur.execute("""
            SELECT id, code, name_ru, description, is_active
            FROM faculties 
            ORDER BY id
        """)
        faculties = cur.fetchall()
        
        print("\n" + "=" * 60)
        print("Оставшиеся факультеты:")
        print("=" * 60)
        for f in faculties:
            print(f"ID: {f['id']}, Код: {f['code']}, Название: {f['name_ru']}")
        
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

