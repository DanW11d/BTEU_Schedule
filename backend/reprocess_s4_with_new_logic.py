"""Переобработка группы S-4 с новой логикой четности недели"""
import os
import sys
import io
import psycopg2
from dotenv import load_dotenv
from excel_parser import ExcelScheduleParser
from pathlib import Path

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

EXCEL_DIR = r"D:\Excel file"

print("=" * 60)
print("ПЕРЕОБРАБОТКА ГРУППЫ S-4 С НОВОЙ ЛОГИКОЙ")
print("=" * 60)

try:
    # Подключаемся к БД
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()
    
    # Получаем ID группы
    cur.execute("SELECT id FROM groups WHERE code = 'S-4'")
    group = cur.fetchone()
    
    if not group:
        print("\n❌ Группа S-4 не найдена!")
        exit(1)
    
    group_id = group[0]
    print(f"\nГруппа S-4, ID: {group_id}\n")
    
    # Удаляем старые занятия
    print("1. Удаление старых занятий...")
    cur.execute("DELETE FROM lessons WHERE group_id = %s", (group_id,))
    deleted_count = cur.rowcount
    conn.commit()
    print(f"   ✅ Удалено занятий: {deleted_count}\n")
    
    # Находим файлы для S-4
    excel_files = []
    if os.path.exists(EXCEL_DIR):
        for file in os.listdir(EXCEL_DIR):
            if file.lower().startswith('s-4') and (file.endswith('.xls') or file.endswith('.xlsx')):
                excel_files.append(os.path.join(EXCEL_DIR, file))
    
    if not excel_files:
        print("❌ Файлы для группы S-4 не найдены!")
        exit(1)
    
    print(f"2. Найдено файлов: {len(excel_files)}\n")
    
    total_parsed = 0
    total_saved = 0
    
    for file_path in excel_files:
        filename = os.path.basename(file_path)
        print(f"   Обработка: {filename}")
        
        try:
            # Парсим файл
            parser = ExcelScheduleParser(file_path)
            lessons = parser.parse('S-4', group_id)
            
            print(f"      Распарсено занятий: {len(lessons)}")
            total_parsed += len(lessons)
            
            # Сохраняем в БД
            saved = 0
            for lesson in lessons:
                try:
                    cur.execute("""
                        INSERT INTO lessons (
                            group_id, day_of_week, lesson_number,
                            subject, teacher, classroom, lesson_type,
                            week_parity, building, notes, is_active, created_at
                        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, TRUE, NOW())
                    """, (
                        lesson['group_id'],
                        lesson['day_of_week'],
                        lesson['lesson_number'],
                        lesson['subject'],
                        lesson.get('teacher'),
                        lesson.get('classroom'),
                        lesson['lesson_type'],
                        lesson['week_parity'],
                        lesson.get('building'),
                        lesson.get('notes')
                    ))
                    saved += 1
                except Exception as e:
                    print(f"      ⚠ Ошибка сохранения занятия: {e}")
            
            conn.commit()
            print(f"      ✅ Сохранено занятий: {saved}")
            total_saved += saved
            
        except Exception as e:
            print(f"      ❌ Ошибка обработки файла: {e}")
            import traceback
            traceback.print_exc()
    
    cur.close()
    conn.close()
    
    print(f"\n{'='*60}")
    print(f"ИТОГО: Распарсено {total_parsed}, Сохранено {total_saved}")
    print(f"{'='*60}")
    
    # Проверяем результат
    print("\n3. Проверка результата...")
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()
    
    # Группируем по четности недели
    cur.execute("""
        SELECT week_parity, COUNT(*) 
        FROM lessons 
        WHERE group_id = %s 
        GROUP BY week_parity
        ORDER BY week_parity
    """, (group_id,))
    
    results = cur.fetchall()
    print("\n   Распределение по неделям:")
    for week_parity, count in results:
        week_name = {'odd': 'Нечетная', 'even': 'Четная', 'both': 'Обе'}.get(week_parity, week_parity)
        print(f"      {week_name}: {count} занятий")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"\n❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

