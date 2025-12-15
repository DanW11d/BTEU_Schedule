"""Анализ логики парсера для выявления причины дубликатов"""
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
print("АНАЛИЗ ЛОГИКИ ПАРСЕРА")
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
    
    # Анализируем строки около "МЕТРОЛОГИЯ"
    print("=" * 60)
    print("АНАЛИЗ СТРОК С МЕТРОЛОГИЯ")
    print("=" * 60)
    
    # Ищем строки с МЕТРОЛОГИЯ
    for row_idx in range(header_row + 1, min(header_row + 30, parser.worksheet.max_row + 1)):
        day_cell = parser.worksheet.cell(row=row_idx, column=1).value
        time_cell = parser.worksheet.cell(row=row_idx, column=2).value
        group_cell = parser.worksheet.cell(row=row_idx, column=group_col).value
        
        if group_cell and 'МЕТРОЛОГИЯ' in str(group_cell).upper():
            print(f"\nСтрока {row_idx}:")
            print(f"  День: '{day_cell}'")
            print(f"  Время: '{time_cell}'")
            print(f"  Группа: '{group_cell}'")
            
            # Показываем соседние строки
            if row_idx > header_row + 1:
                prev_day = parser.worksheet.cell(row=row_idx-1, column=1).value
                prev_time = parser.worksheet.cell(row=row_idx-1, column=2).value
                prev_group = parser.worksheet.cell(row=row_idx-1, column=group_col).value
                print(f"  Предыдущая строка {row_idx-1}:")
                print(f"    День: '{prev_day}', Время: '{prev_time}', Группа: '{prev_group}'")
            
            if row_idx < parser.worksheet.max_row:
                next_day = parser.worksheet.cell(row=row_idx+1, column=1).value
                next_time = parser.worksheet.cell(row=row_idx+1, column=2).value
                next_group = parser.worksheet.cell(row=row_idx+1, column=group_col).value
                print(f"  Следующая строка {row_idx+1}:")
                print(f"    День: '{next_day}', Время: '{next_time}', Группа: '{next_group}'")
    
    # Теперь парсим и смотрим, что получается
    print("\n" + "=" * 60)
    print("РЕЗУЛЬТАТ ПАРСИНГА")
    print("=" * 60)
    
    # Используем фиктивный group_id для теста
    lessons = parser.parse(group_id=51, group_code='S-4')
    
    # Фильтруем только понедельник четной недели
    monday_even = [l for l in lessons if l.get('day_of_week') == 1 and l.get('week_parity') == 'even']
    
    print(f"\nПонедельник, четная неделя: {len(monday_even)} занятий\n")
    
    for lesson in sorted(monday_even, key=lambda x: x.get('lesson_number', 0)):
        print(f"Пара {lesson.get('lesson_number')}: {lesson.get('subject', 'N/A')[:60]}...")
        print(f"  ID в результате: {lesson.get('id', 'N/A')}")
        print(f"  Преподаватель: {lesson.get('teacher', 'N/A')}")
        print(f"  Аудитория: {lesson.get('classroom', 'N/A')}")
        print()
        
except Exception as e:
    print(f"Ошибка: {e}")
    import traceback
    traceback.print_exc()

