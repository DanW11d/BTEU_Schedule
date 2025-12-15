"""Удаление S-41 и обновление S-4: специальность 'Инженеры-программисты'"""
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
    
    # 1. Удаляем группу S-41
    print("\n1. Удаление группы S-41...")
    cur.execute("SELECT id, code, specialization FROM groups WHERE code = 'S-41'")
    s41 = cur.fetchone()
    
    if s41:
        s41_id, s41_code, s41_spec = s41
        print(f"   Найдена группа S-41 (ID: {s41_id}, специальность: {s41_spec})")
        
        # Проверяем, есть ли занятия у этой группы
        cur.execute("SELECT COUNT(*) FROM lessons WHERE group_id = %s", (s41_id,))
        lessons_count = cur.fetchone()[0]
        
        if lessons_count > 0:
            print(f"   ⚠ У группы S-41 есть {lessons_count} занятий(я)")
            print("   Удаляем занятия...")
            cur.execute("DELETE FROM lessons WHERE group_id = %s", (s41_id,))
        
        # Удаляем группу
        cur.execute("DELETE FROM groups WHERE id = %s", (s41_id,))
        conn.commit()
        print(f"   ✅ Группа S-41 удалена")
    else:
        print("   ℹ Группа S-41 не найдена (уже удалена или не существовала)")
    
    # 2. Обновляем группу S-4
    print("\n2. Обновление группы S-4...")
    cur.execute("SELECT id, code, name, specialization FROM groups WHERE code = 'S-4'")
    s4 = cur.fetchone()
    
    if not s4:
        print("   ❌ Группа S-4 не найдена!")
        cur.close()
        conn.close()
        exit(1)
    
    s4_id, s4_code, s4_name, s4_spec = s4
    print(f"   Текущая специальность: {s4_spec}")
    
    # Обновляем специальность
    cur.execute("""
        UPDATE groups 
        SET specialization = %s
        WHERE id = %s
    """, ('Инженеры-программисты', s4_id))
    conn.commit()
    
    print(f"   ✅ Специальность обновлена на: Инженеры-программисты")
    
    # 3. Показываем все группы 4 курса ФКИФ
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
            marker = " ← S-4 (обновлена)" if code == 'S-4' else ""
            print(f"  {code} - {spec}{marker}")
    else:
        print("\n❌ Групп не найдено!")
    
    cur.close()
    conn.close()
    
    print("\n" + "=" * 60)
    print("ОБНОВЛЕНИЕ ЗАВЕРШЕНО")
    print("=" * 60)
    print("\n✅ Группа S-41 удалена")
    print("✅ Группа S-4 обновлена: специальность 'Инженеры-программисты'")
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

