"""Анализ определения четности недели в парсере"""
import os
import sys
import io
from excel_parser_v2 import ExcelScheduleParserV2

# Исправление кодировки для Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

EXCEL_DIR = r"D:\Excel file"
filename = "s-4 (28.08.25).xls"
filepath = os.path.join(EXCEL_DIR, filename)

print("=" * 60)
print("АНАЛИЗ ОПРЕДЕЛЕНИЯ ЧЕТНОСТИ НЕДЕЛИ")
print("=" * 60)

try:
    parser = ExcelScheduleParserV2(filepath)
    parser.load_file()
    
    # Находим столбец группы
    group_col = parser._find_group_column('S-4')
    if not group_col:
        print("Группа S-4 не найдена!")
        exit(1)
    
    print(f"\nСтолбец группы S-4: {group_col}\n")
    
    # Находим строку заголовка
    header_row = parser._find_header_row()
    print(f"Строка заголовка: {header_row}\n")
    
    # Анализируем строки понедельника
    print("=" * 60)
    print("АНАЛИЗ ПОНЕДЕЛЬНИКА")
    print("=" * 60)
    
    current_day = None
    current_week_parity = None
    
    for row_idx in range(header_row + 1, min(header_row + 50, parser.worksheet.max_row + 1)):
        day_cell = parser.worksheet.cell(row=row_idx, column=1).value
        time_cell = parser.worksheet.cell(row=row_idx, column=2).value
        group_cell = parser.worksheet.cell(row=row_idx, column=group_col).value
        
        # Определяем день
        if day_cell:
            day_str = str(day_cell).strip().lower()
            if day_str in parser.DAYS_OF_WEEK:
                current_day = parser.DAYS_OF_WEEK[day_str]
                if current_day == 1:  # Понедельник
                    print(f"\nСтрока {row_idx}: Найден понедельник")
        
        # Определяем четность недели из ячейки группы
        if group_cell and current_day == 1:
            cell_str = str(group_cell).strip()
            week_parity = parser._extract_week_parity(cell_str)
            
            if time_cell and parser._is_time_format(str(time_cell).strip()):
                time_str = str(time_cell).strip()
                print(f"  Строка {row_idx}: Время {time_str}, Неделя: {week_parity}")
                if cell_str:
                    print(f"    Содержимое: '{cell_str[:80]}...'")
    
    # Теперь парсим и смотрим результат
    print("\n" + "=" * 60)
    print("РЕЗУЛЬТАТ ПАРСИНГА ПОНЕДЕЛЬНИКА")
    print("=" * 60)
    
    lessons = parser.parse(group_id=51, group_code='S-4')
    
    # Фильтруем понедельник
    monday_lessons = [l for l in lessons if l.get('day_of_week') == 1]
    
    print(f"\nВсего занятий в понедельник: {len(monday_lessons)}\n")
    
    # Группируем по четности недели
    odd_lessons = [l for l in monday_lessons if l.get('week_parity') == 'odd']
    even_lessons = [l for l in monday_lessons if l.get('week_parity') == 'even']
    both_lessons = [l for l in monday_lessons if l.get('week_parity') == 'both']
    
    print(f"Нечетная неделя: {len(odd_lessons)} занятий")
    for lesson in sorted(odd_lessons, key=lambda x: x.get('lesson_number', 0)):
        print(f"  Пара {lesson.get('lesson_number')}: {lesson.get('subject', 'N/A')[:50]}...")
    
    print(f"\nЧетная неделя: {len(even_lessons)} занятий")
    for lesson in sorted(even_lessons, key=lambda x: x.get('lesson_number', 0)):
        print(f"  Пара {lesson.get('lesson_number')}: {lesson.get('subject', 'N/A')[:50]}...")
    
    print(f"\nОбе недели: {len(both_lessons)} занятий")
    for lesson in sorted(both_lessons, key=lambda x: x.get('lesson_number', 0)):
        print(f"  Пара {lesson.get('lesson_number')}: {lesson.get('subject', 'N/A')[:50]}...")
        
except Exception as e:
    print(f"Ошибка: {e}")
    import traceback
    traceback.print_exc()

