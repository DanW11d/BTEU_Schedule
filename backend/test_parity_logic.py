"""Тест логики определения четности недели"""
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
print("ТЕСТ ЛОГИКИ ОПРЕДЕЛЕНИЯ ЧЕТНОСТИ")
print("=" * 60)

try:
    parser = ExcelScheduleParserV2(filepath)
    parser.load_file()
    
    group_col = parser._find_group_column('S-4')
    header_row = parser._find_header_row()
    
    print(f"\nСтолбец группы: {group_col}")
    print(f"Строка заголовка: {header_row}\n")
    
    # Симулируем парсинг и отслеживаем четность
    current_day = None
    current_week_parity = None
    separator_count = 0
    
    print("=" * 60)
    print("ОТСЛЕЖИВАНИЕ ЧЕТНОСТИ ПРИ ПАРСИНГЕ")
    print("=" * 60)
    
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
                    print(f"\nСтрока {row_idx}: ПОНЕДЕЛЬНИК")
                    separator_count = 0
        
        if current_day == 1:
            if group_cell:
                cell_str = str(group_cell).strip()
                clean_str = cell_str.replace(' ', '').replace('─', '').replace('—', '').replace('-', '')
                is_sep = (cell_str.startswith('—') or cell_str.startswith('─') or 
                         (len(clean_str) < 3 and ('—' in cell_str or '─' in cell_str)))
                
                if is_sep:
                    separator_count += 1
                    # Симулируем логику переключения
                    if current_week_parity is None:
                        current_week_parity = 'odd'
                    elif current_week_parity == 'odd':
                        current_week_parity = 'even'
                    else:
                        current_week_parity = 'odd'
                    print(f"  Строка {row_idx}: РАЗДЕЛИТЕЛЬ #{separator_count} → Четность: {current_week_parity}")
                elif time_cell and parser._is_time_format(str(time_cell).strip()):
                    time_str = str(time_cell).strip()
                    week_parity_to_use = current_week_parity if current_week_parity is not None else 'odd'
                    print(f"  Строка {row_idx}: Время {time_str}, Четность: {week_parity_to_use}")
        
        if day_cell and current_day and current_day != 1:
            break
    
    # Теперь парсим и смотрим результат
    print("\n" + "=" * 60)
    print("РЕЗУЛЬТАТ ПАРСИНГА")
    print("=" * 60)
    
    lessons = parser.parse(group_id=51, group_code='S-4')
    monday_lessons = [l for l in lessons if l.get('day_of_week') == 1]
    
    odd_count = len([l for l in monday_lessons if l.get('week_parity') == 'odd'])
    even_count = len([l for l in monday_lessons if l.get('week_parity') == 'even'])
    both_count = len([l for l in monday_lessons if l.get('week_parity') == 'both'])
    
    print(f"\nПонедельник:")
    print(f"  Нечетная: {odd_count} занятий")
    print(f"  Четная: {even_count} занятий")
    print(f"  Обе: {both_count} занятий")
    
    if even_count > 0:
        print(f"\n⚠ Внимание: Найдено {even_count} занятий для четной недели")
        print("   Это означает, что в файле есть разделители, которые переключают четность")
        
except Exception as e:
    print(f"Ошибка: {e}")
    import traceback
    traceback.print_exc()

