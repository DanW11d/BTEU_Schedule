"""
Скрипт для обновления факультетов и кафедр в базе данных
Структура согласно сайту университета
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

def update_faculties_and_departments():
    """Обновляет факультеты и кафедры в БД"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()
        
        print("Начинаю обновление факультетов и кафедр...")
        
        # 1. Обновляем/добавляем факультеты
        faculties = [
            {
                'code': 'FEU',
                'name_ru': 'Факультет экономики и управления',
                'name_en': 'Faculty of Economics and Management',
                'description': 'Факультет экономики и управления'
            },
            {
                'code': 'FKF',
                'name_ru': 'Факультет коммерции и финансов',
                'name_en': 'Faculty of Commerce and Finance',
                'description': 'Факультет коммерции и финансов'
            },
            {
                'code': 'FPKP',
                'name_ru': 'Факультет повышения квалификации и переподготовки',
                'name_en': 'Faculty of Advanced Training and Retraining',
                'description': 'Факультет повышения квалификации и переподготовки'
            }
        ]
        
        faculty_ids = {}
        
        for faculty in faculties:
            cur.execute("""
                INSERT INTO faculties (code, name_ru, name_en, description, is_active)
                VALUES (%s, %s, %s, %s, TRUE)
                ON CONFLICT (code) DO UPDATE 
                SET name_ru = EXCLUDED.name_ru,
                    name_en = EXCLUDED.name_en,
                    description = EXCLUDED.description,
                    is_active = TRUE
                RETURNING id
            """, (faculty['code'], faculty['name_ru'], faculty['name_en'], faculty['description']))
            
            faculty_id = cur.fetchone()[0]
            faculty_ids[faculty['code']] = faculty_id
            print(f"[OK] Факультет: {faculty['name_ru']} (ID: {faculty_id})")
        
        # 2. Обновляем/добавляем кафедры
        departments = [
            # Факультет экономики и управления (FEU)
            {
                'code': 'IVS',
                'name_ru': 'Кафедра информационно-вычислительных систем',
                'name_en': 'Department of Information and Computing Systems',
                'faculty_code': 'FEU',
                'description': 'Кафедра информационно-вычислительных систем'
            },
            {
                'code': 'PET',
                'name_ru': 'Кафедра права и экономических теорий',
                'name_en': 'Department of Law and Economic Theories',
                'faculty_code': 'FEU',
                'description': 'Кафедра права и экономических теорий'
            },
            {
                'code': 'MNE',
                'name_ru': 'Кафедра мировой и национальной экономики',
                'name_en': 'Department of World and National Economics',
                'faculty_code': 'FEU',
                'description': 'Кафедра мировой и национальной экономики'
            },
            {
                'code': 'ET',
                'name_ru': 'Кафедра экономики торговли',
                'name_en': 'Department of Trade Economics',
                'faculty_code': 'FEU',
                'description': 'Кафедра экономики торговли'
            },
            # Факультет коммерции и финансов (FKF)
            {
                'code': 'BAF',
                'name_ru': 'Кафедра бухгалтерского учета и финансов',
                'name_en': 'Department of Accounting and Finance',
                'faculty_code': 'FKF',
                'description': 'Кафедра бухгалтерского учета и финансов'
            },
            {
                'code': 'GPE',
                'name_ru': 'Кафедра гуманитарного и физического воспитания',
                'name_en': 'Department of Humanities and Physical Education',
                'faculty_code': 'FKF',
                'description': 'Кафедра гуманитарного и физического воспитания'
            },
            {
                'code': 'CL',
                'name_ru': 'Кафедра коммерции и логистики',
                'name_en': 'Department of Commerce and Logistics',
                'faculty_code': 'FKF',
                'description': 'Кафедра коммерции и логистики'
            },
            {
                'code': 'MKT',
                'name_ru': 'Кафедра маркетинга',
                'name_en': 'Department of Marketing',
                'faculty_code': 'FKF',
                'description': 'Кафедра маркетинга'
            },
            {
                'code': 'TD',
                'name_ru': 'Кафедра товароведения',
                'name_en': 'Department of Commodity Science',
                'faculty_code': 'FKF',
                'description': 'Кафедра товароведения'
            },
            # Факультет повышения квалификации и переподготовки (FPKP)
            {
                'code': 'EPD',
                'name_ru': 'Кафедра экономических и правовых дисциплин',
                'name_en': 'Department of Economic and Legal Disciplines',
                'faculty_code': 'FPKP',
                'description': 'Кафедра экономических и правовых дисциплин'
            }
        ]
        
        for dept in departments:
            faculty_id = faculty_ids[dept['faculty_code']]
            cur.execute("""
                INSERT INTO departments (code, name_ru, name_en, faculty_id, description, is_active)
                VALUES (%s, %s, %s, %s, %s, TRUE)
                ON CONFLICT (code) DO UPDATE 
                SET name_ru = EXCLUDED.name_ru,
                    name_en = EXCLUDED.name_en,
                    faculty_id = EXCLUDED.faculty_id,
                    description = EXCLUDED.description,
                    is_active = TRUE
            """, (dept['code'], dept['name_ru'], dept['name_en'], faculty_id, dept['description']))
            print(f"  [OK] Кафедра: {dept['name_ru']} (факультет: {dept['faculty_code']})")
        
        # Сохраняем изменения
        conn.commit()
        print("\n[OK] Все факультеты и кафедры успешно обновлены!")
        
        # Выводим результат
        print("\nТекущая структура:")
        cur.execute("""
            SELECT 
                f.code as faculty_code,
                f.name_ru as faculty_name,
                d.code as department_code,
                d.name_ru as department_name
            FROM faculties f
            LEFT JOIN departments d ON d.faculty_id = f.id
            WHERE f.is_active = TRUE
            ORDER BY f.code, d.code
        """)
        
        current_faculty = None
        for row in cur.fetchall():
            faculty_code, faculty_name, dept_code, dept_name = row
            if current_faculty != faculty_code:
                print(f"\n[FACULTY] {faculty_name} ({faculty_code})")
                current_faculty = faculty_code
            if dept_code:
                print(f"   - {dept_name} ({dept_code})")
        
        cur.close()
        conn.close()
        
    except Exception as e:
        print(f"[ERROR] Ошибка: {e}")
        if conn:
            conn.rollback()
            conn.close()
        raise

if __name__ == '__main__':
    update_faculties_and_departments()

