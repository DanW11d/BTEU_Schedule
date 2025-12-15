"""Обновление специальностей групп: замена 'специализация' на 'специальность' и добавление S-41"""
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
    print("ОБНОВЛЕНИЕ СПЕЦИАЛЬНОСТЕЙ ГРУПП")
    print("=" * 60)
    
    # 1. Заменяем "Специализация" на "Специальность" у всех групп
    print("\n1. Замена 'Специализация' на 'Специальность'...")
    cur.execute("""
        UPDATE groups 
        SET specialization = REPLACE(specialization, 'Специализация', 'Специальность')
        WHERE specialization LIKE '%Специализация%'
    """)
    updated_count = cur.rowcount
    conn.commit()
    print(f"   Обновлено групп: {updated_count}")
    
    # 2. Проверяем группы с "специализация" в названии
    cur.execute("""
        SELECT code, specialization 
        FROM groups 
        WHERE specialization ILIKE '%специализация%'
        ORDER BY code
    """)
    remaining = cur.fetchall()
    if remaining:
        print(f"\n   Найдено групп, которые еще содержат 'специализация': {len(remaining)}")
        for code, spec in remaining:
            new_spec = spec.replace('специализация', 'специальность').replace('Специализация', 'Специальность')
            cur.execute("UPDATE groups SET specialization = %s WHERE code = %s", (new_spec, code))
            print(f"     {code}: '{spec}' -> '{new_spec}'")
        conn.commit()
    
    # 3. Находим факультет ФКИФ для группы S-41
    cur.execute("SELECT id, code, name_ru FROM faculties WHERE code = 'FKIF'")
    fkif = cur.fetchone()
    
    if not fkif:
        print("\n❌ Факультет ФКИФ не найден!")
        cur.close()
        conn.close()
        exit(1)
    
    fkif_id, fkif_code, fkif_name = fkif
    print(f"\n2. Факультет для S-41: {fkif_code} ({fkif_name}) [ID: {fkif_id}]")
    
    # 4. Проверяем, есть ли уже группа S-41
    cur.execute("SELECT id, code, name, course, education_form, faculty_id, specialization FROM groups WHERE code = 'S-41'")
    s41 = cur.fetchone()
    
    if s41:
        s41_id, s41_code, s41_name, s41_course, s41_form, s41_faculty_id, s41_spec = s41
        print(f"\n3. Группа S-41 уже существует:")
        print(f"   ID: {s41_id}")
        print(f"   Специальность: {s41_spec}")
        print(f"   Факультет ID: {s41_faculty_id}")
        
        # Обновляем специальность и факультет
        cur.execute("""
            UPDATE groups 
            SET specialization = %s, faculty_id = %s, course = %s, education_form = %s
            WHERE id = %s
        """, ('Инженеры-программисты', fkif_id, 4, 'full_time', s41_id))
        conn.commit()
        print(f"\n   ✅ Группа S-41 обновлена!")
        print(f"   Специальность: Инженеры-программисты")
        print(f"   Факультет: {fkif_code}")
        print(f"   Курс: 4")
        print(f"   Форма: очная (full_time)")
    else:
        # Создаем группу S-41
        print(f"\n3. Создание группы S-41...")
        cur.execute("""
            INSERT INTO groups (code, name, course, education_form, faculty_id, specialization, is_active, student_count)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            RETURNING id
        """, (
            'S-41',
            'Группа S-41',
            4,
            'full_time',
            fkif_id,
            'Инженеры-программисты',
            True,
            0
        ))
        new_id = cur.fetchone()[0]
        conn.commit()
        print(f"   ✅ Группа S-41 создана! ID: {new_id}")
        print(f"   Специальность: Инженеры-программисты")
        print(f"   Факультет: {fkif_code}")
        print(f"   Курс: 4")
        print(f"   Форма: очная (full_time)")
    
    # 5. Показываем все группы 4 курса ФКИФ
    print("\n" + "=" * 60)
    print("ВСЕ ГРУППЫ 4 КУРСА ФКИФ (ОЧНИКИ)")
    print("=" * 60)
    cur.execute("""
        SELECT 
            g.code,
            g.name,
            g.specialization
        FROM groups g
        JOIN faculties f ON g.faculty_id = f.id
        WHERE g.course = 4 
            AND g.education_form = 'full_time'
            AND f.code = 'FKIF'
            AND g.is_active = TRUE
        ORDER BY g.code
    """)
    
    groups = cur.fetchall()
    if groups:
        print(f"\nНайдено групп: {len(groups)}\n")
        for group in groups:
            code, name, spec = group
            marker = " ← S-41" if code == 'S-41' else (" ← S-4" if code == 'S-4' else "")
            print(f"  {code} - {spec}{marker}")
    else:
        print("\n❌ Групп не найдено!")
    
    cur.close()
    conn.close()
    
    print("\n" + "=" * 60)
    print("ОБНОВЛЕНИЕ ЗАВЕРШЕНО")
    print("=" * 60)
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

