"""Отладка структуры Excel файла для понимания, почему предмет разбит"""
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
print("СТРУКТУРА EXCEL ФАЙЛА (около пара 3-4)")
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
    
    # Ищем строки с "МЕТРОЛОГИЯ" и "ТЕХНОЛОГИЯХ"
    print("Строки с МЕТРОЛОГИЯ или ТЕХНОЛОГИЯХ:\n")
    found_metrology = False
    
    for row_idx in range(1, min(parser.worksheet.max_row + 1, 100)):
        day_cell = parser.worksheet.cell(row=row_idx, column=1).value
        time_cell = parser.worksheet.cell(row=row_idx, column=2).value
        group_cell = parser.worksheet.cell(row=row_idx, column=group_col).value
        
        if group_cell:
            cell_str = str(group_cell).strip().upper()
            if 'МЕТРОЛОГИЯ' in cell_str or 'ТЕХНОЛОГИЯХ' in cell_str:
                found_metrology = True
                print(f"Строка {row_idx}:")
                print(f"  День: '{day_cell}'")
                print(f"  Время: '{time_cell}'")
                print(f"  Группа: '{group_cell}'")
                
                # Показываем соседние строки для контекста
                if row_idx > 1:
                    prev_day = parser.worksheet.cell(row=row_idx-1, column=1).value
                    prev_time = parser.worksheet.cell(row=row_idx-1, column=2).value
                    prev_group = parser.worksheet.cell(row=row_idx-1, column=group_col).value
                    print(f"  Предыдущая строка {row_idx-1}: день='{prev_day}', время='{prev_time}', группа='{prev_group}'")
                
                if row_idx < parser.worksheet.max_row:
                    next_day = parser.worksheet.cell(row=row_idx+1, column=1).value
                    next_time = parser.worksheet.cell(row=row_idx+1, column=2).value
                    next_group = parser.worksheet.cell(row=row_idx+1, column=group_col).value
                    print(f"  Следующая строка {row_idx+1}: день='{next_day}', время='{next_time}', группа='{next_group}'")
                print("-" * 60)
    
    if not found_metrology:
        print("Строки с МЕТРОЛОГИЯ или ТЕХНОЛОГИЯХ не найдены!")
        
except Exception as e:
    print(f"Ошибка: {e}")
    import traceback
    traceback.print_exc()

