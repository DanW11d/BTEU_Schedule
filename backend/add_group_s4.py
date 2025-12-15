"""Добавление группы S-4 в БД"""
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
    print("ДОБАВЛЕНИЕ ГРУППЫ S-4 В БД")
    print("=" * 60)
    
    # Проверяем, есть ли уже группа S-4
    cur.execute("SELECT id, code, name, course FROM groups WHERE code = 'S-4'")
    existing = cur.fetchone()
    
    if existing:
        print(f"\n⚠ Группа S-4 уже существует!")
        print(f"  ID: {existing[0]}")
        print(f"  Код: {existing[1]}")
        print(f"  Название: {existing[2]}")
        print(f"  Курс: {existing[3]}")
    else:
        # Ищем похожие группы 4 курса для определения параметров
        cur.execute("""
            SELECT g.code, g.name, g.course, g.education_form, g.faculty_id, g.specialization
            FROM groups g
            WHERE g.course = 4 AND g.education_form = 'full_time'
            ORDER BY g.code
            LIMIT 1
        """)
        sample = cur.fetchone()
        
        if sample:
            sample_code, sample_name, sample_course, sample_form, sample_faculty, sample_spec = sample
            print(f"\nНайдена похожая группа для примера: {sample_code}")
            print(f"  Факультет ID: {sample_faculty}")
            print(f"  Форма: {sample_form}")
            print(f"  Курс: {sample_course}")
            
            # Получаем название факультета
            cur.execute("SELECT code, name_ru FROM faculties WHERE id = %s", (sample_faculty,))
            faculty = cur.fetchone()
            faculty_code = faculty[0] if faculty else None
            
            # Добавляем группу S-4
            # Нужно определить факультет - обычно группы 4 курса на одном факультете
            # Для S-4 предположим, что это тот же факультет, что и у других групп 4 курса
            
            specialization = "Специализация S-4"  # Можно будет уточнить позже
            
            cur.execute("""
                INSERT INTO groups (code, name, course, education_form, faculty_id, specialization, is_active, student_count)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                RETURNING id
            """, (
                'S-4',
                'Группа S-4',
                4,
                'full_time',
                sample_faculty,
                specialization,
                True,
                0
            ))
            
            new_id = cur.fetchone()[0]
            conn.commit()
            
            print(f"\n✅ Группа S-4 успешно добавлена!")
            print(f"  ID: {new_id}")
            print(f"  Код: S-4")
            print(f"  Название: Группа S-4")
            print(f"  Курс: 4")
            print(f"  Форма: full_time")
            print(f"  Факультет ID: {sample_faculty}")
            print(f"  Специализация: {specialization}")
        else:
            print("\n❌ Не найдено групп 4 курса для определения параметров!")
            print("Нужно вручную указать факультет для группы S-4")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

