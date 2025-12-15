"""Обновление группы S-4: установка факультета ФКИФ"""
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
    print("ОБНОВЛЕНИЕ ГРУППЫ S-4")
    print("=" * 60)
    
    # Находим факультет ФКИФ
    cur.execute("SELECT id, code, name_ru FROM faculties WHERE code = 'FKIF' OR code = 'ФКИФ'")
    fkif = cur.fetchone()
    
    if not fkif:
        print("\n❌ Факультет ФКИФ не найден!")
        print("\nДоступные факультеты:")
        cur.execute("SELECT id, code, name_ru FROM faculties WHERE is_active = TRUE ORDER BY code")
        for fac in cur.fetchall():
            print(f"  {fac[1]} - {fac[2]} (ID: {fac[0]})")
    else:
        fkif_id, fkif_code, fkif_name = fkif
        print(f"\n✅ Факультет найден: {fkif_code} ({fkif_name}) [ID: {fkif_id}]")
        
        # Проверяем группу S-4
        cur.execute("SELECT id, code, name, course, education_form, faculty_id FROM groups WHERE code = 'S-4'")
        s4 = cur.fetchone()
        
        if not s4:
            print("\n❌ Группа S-4 не найдена! Создаем...")
            # Создаем группу S-4
            cur.execute("""
                INSERT INTO groups (code, name, course, education_form, faculty_id, specialization, is_active, student_count)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                RETURNING id
            """, (
                'S-4',
                'Группа S-4',
                4,
                'full_time',
                fkif_id,
                'Специализация S-4',
                True,
                0
            ))
            new_id = cur.fetchone()[0]
            conn.commit()
            print(f"✅ Группа S-4 создана! ID: {new_id}")
        else:
            s4_id, s4_code, s4_name, s4_course, s4_form, s4_faculty_id = s4
            print(f"\n✅ Группа S-4 найдена:")
            print(f"  ID: {s4_id}")
            print(f"  Код: {s4_code}")
            print(f"  Текущий факультет ID: {s4_faculty_id}")
            
            if s4_faculty_id != fkif_id:
                # Обновляем факультет
                cur.execute("""
                    UPDATE groups 
                    SET faculty_id = %s
                    WHERE id = %s
                """, (fkif_id, s4_id))
                conn.commit()
                print(f"\n✅ Факультет обновлен на {fkif_code} ({fkif_name})")
            else:
                print(f"\n✅ Факультет уже установлен правильно: {fkif_code}")
        
        # Проверяем все группы 4 курса очников
        print("\n" + "=" * 60)
        print("ВСЕ ГРУППЫ 4 КУРСА (ОЧНИКИ)")
        print("=" * 60)
        cur.execute("""
            SELECT 
                g.code,
                g.name,
                f.code as faculty_code,
                f.name_ru as faculty_name
            FROM groups g
            JOIN faculties f ON g.faculty_id = f.id
            WHERE g.course = 4 
                AND g.education_form = 'full_time'
                AND g.is_active = TRUE
            ORDER BY g.code
        """)
        
        groups = cur.fetchall()
        if groups:
            print(f"\nНайдено групп: {len(groups)}\n")
            for group in groups:
                code, name, fac_code, fac_name = group
                marker = " ← S-4" if code == 'S-4' else ""
                print(f"  {code} - {name} ({fac_code}){marker}")
        else:
            print("\n❌ Групп не найдено!")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

