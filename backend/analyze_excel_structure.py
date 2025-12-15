"""Анализ структуры Excel файла для понимания четности недели"""
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
print("АНАЛИЗ СТРУКТУРЫ EXCEL ФАЙЛА")
print("=" * 60)

try:
    parser = ExcelScheduleParserV2(filepath)
    parser.load_file()
    
    # Находим столбец группы
    group_col = parser._find_group_column('S-4')
    header_row = parser._find_header_row()
    
    print(f"\nСтолбец группы: {group_col}")
    print(f"Строка заголовка: {header_row}\n")
    
    # Анализируем структуру понедельника
    print("=" * 60)
    print("СТРУКТУРА ПОНЕДЕЛЬНИКА (первые 30 строк после заголовка)")
    print("=" * 60)
    
    current_day = None
    row_num = 0
    
    for row_idx in range(header_row + 1, min(header_row + 30, parser.worksheet.max_row + 1)):
        day_cell = parser.worksheet.cell(row=row_idx, column=1).value
        time_cell = parser.worksheet.cell(row=row_idx, column=2).value
        group_cell = parser.worksheet.cell(row=row_idx, column=group_col).value
        
        # Определяем день
        if day_cell:
            day_str = str(day_cell).strip().lower()
            if day_str in parser.DAYS_OF_WEEK:
                current_day = parser.DAYS_OF_WEEK[day_str]
                if current_day == 1:  # Понедельник
                    print(f"\n{'='*60}")
                    print(f"Строка {row_idx}: НАЙДЕН ПОНЕДЕЛЬНИК")
                    print(f"{'='*60}")
        
        if current_day == 1:
            row_num += 1
            time_str = str(time_cell).strip() if time_cell else ""
            group_str = str(group_cell).strip() if group_cell else ""
            
            # Проверяем на разделитель
            is_separator = False
            if group_str:
                if '─' in group_str or '—' in group_str or len(group_str.replace(' ', '').replace('─', '').replace('—', '')) < 3:
                    is_separator = True
            
            print(f"\nСтрока {row_idx} (ряд {row_num}):")
            print(f"  День: '{day_cell}'")
            print(f"  Время: '{time_str}'")
            print(f"  Группа: '{group_str[:80]}...'")
            
            if is_separator:
                print(f"  ⚠ РАЗДЕЛИТЕЛЬ НЕДЕЛЬ")
            
            # Ищем паттерны четности
            if group_str:
                week_match = parser._extract_week_parity(group_str)
                print(f"  Определена четность: {week_match}")
                
                # Ищем номера недель
                import re
                week_nums = re.findall(r'\d+', group_str)
                if week_nums:
                    print(f"  Найдены числа: {week_nums}")
        
        # Если перешли на другой день, останавливаемся
        if day_cell and current_day and current_day != 1:
            break
        
except Exception as e:
    print(f"Ошибка: {e}")
    import traceback
    traceback.print_exc()

