"""Анализ второго файла для понимания структуры"""
import os
import sys
import io
from excel_parser_v2 import ExcelScheduleParserV2

# Исправление кодировки для Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

EXCEL_DIR = r"D:\Excel file"
filename = "s-4-s-15.09.25 (12.09.25).xls"
filepath = os.path.join(EXCEL_DIR, filename)

print("=" * 60)
print("АНАЛИЗ ВТОРОГО ФАЙЛА")
print("=" * 60)

try:
    parser = ExcelScheduleParserV2(filepath)
    parser.load_file()
    
    group_col = parser._find_group_column('S-4')
    header_row = parser._find_header_row()
    
    print(f"\nСтолбец группы: {group_col}")
    print(f"Строка заголовка: {header_row}\n")
    
    # Ищем все разделители
    print("=" * 60)
    print("ВСЕ РАЗДЕЛИТЕЛИ В ФАЙЛЕ")
    print("=" * 60)
    
    separators = []
    current_day = None
    
    for row_idx in range(header_row + 1, min(header_row + 100, parser.worksheet.max_row + 1)):
        day_cell = parser.worksheet.cell(row=row_idx, column=1).value
        group_cell = parser.worksheet.cell(row=row_idx, column=group_col).value
        
        if day_cell:
            day_str = str(day_cell).strip().lower()
            if day_str in parser.DAYS_OF_WEEK:
                current_day = parser.DAYS_OF_WEEK[day_str]
        
        if group_cell:
            group_str = str(group_cell).strip()
            clean_str = group_str.replace(' ', '').replace('─', '').replace('—', '').replace('-', '')
            
            is_separator = False
            if group_str:
                if (group_str.startswith('—') or group_str.startswith('─') or 
                    (len(clean_str) < 3 and ('—' in group_str or '─' in group_str or group_str.count('-') > 5))):
                    is_separator = True
            
            if is_separator:
                day_name = ['ПН', 'ВТ', 'СР', 'ЧТ', 'ПТ', 'СБ'][current_day - 1] if current_day else '?'
                separators.append((row_idx, current_day, day_name))
                print(f"Строка {row_idx}: Разделитель (День {current_day} - {day_name})")
    
    print(f"\nВсего разделителей: {len(separators)}")
    
    # Парсим и смотрим результат
    print("\n" + "=" * 60)
    print("РЕЗУЛЬТАТ ПАРСИНГА")
    print("=" * 60)
    
    lessons = parser.parse(group_id=51, group_code='S-4')
    
    # Группируем по четности
    odd_count = len([l for l in lessons if l.get('week_parity') == 'odd'])
    even_count = len([l for l in lessons if l.get('week_parity') == 'even'])
    both_count = len([l for l in lessons if l.get('week_parity') == 'both'])
    
    print(f"\nВсего занятий: {len(lessons)}")
    print(f"  Нечетная: {odd_count}")
    print(f"  Четная: {even_count}")
    print(f"  Обе: {both_count}")
    
    # Показываем примеры занятий четной недели
    if even_count > 0:
        print(f"\nПримеры занятий четной недели:")
        even_lessons = [l for l in lessons if l.get('week_parity') == 'even']
        for i, lesson in enumerate(even_lessons[:5], 1):
            print(f"  {i}. День {lesson.get('day_of_week')}, Пара {lesson.get('lesson_number')}: {lesson.get('subject', 'N/A')[:50]}...")
        
except Exception as e:
    print(f"Ошибка: {e}")
    import traceback
    traceback.print_exc()

