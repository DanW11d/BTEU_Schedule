"""Поиск всех разделителей в Excel файле"""
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
print("ПОИСК ВСЕХ РАЗДЕЛИТЕЛЕЙ В EXCEL ФАЙЛЕ")
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
        time_cell = parser.worksheet.cell(row=row_idx, column=2).value
        group_cell = parser.worksheet.cell(row=row_idx, column=group_col).value
        
        # Определяем день
        if day_cell:
            day_str = str(day_cell).strip().lower()
            if day_str in parser.DAYS_OF_WEEK:
                current_day = parser.DAYS_OF_WEEK[day_str]
        
        if group_cell:
            group_str = str(group_cell).strip()
            clean_str = group_str.replace(' ', '').replace('─', '').replace('—', '').replace('-', '')
            
            # Проверяем на разделитель
            is_separator = False
            if group_str:
                if (group_str.startswith('—') or group_str.startswith('─') or 
                    (len(clean_str) < 3 and ('—' in group_str or '─' in group_str or group_str.count('-') > 5))):
                    is_separator = True
            
            if is_separator:
                day_name = ['ПН', 'ВТ', 'СР', 'ЧТ', 'ПТ', 'СБ'][current_day - 1] if current_day else '?'
                separators.append((row_idx, current_day, day_name, time_cell))
                print(f"Строка {row_idx}: Разделитель (День {current_day} - {day_name}, Время: {time_cell})")
    
    print(f"\nВсего разделителей: {len(separators)}")
    
    # Анализируем структуру понедельника с учетом разделителей
    print("\n" + "=" * 60)
    print("СТРУКТУРА ПОНЕДЕЛЬНИКА С РАЗДЕЛИТЕЛЯМИ")
    print("=" * 60)
    
    monday_separators = [s for s in separators if s[1] == 1]
    print(f"\nРазделителей в понедельнике: {len(monday_separators)}")
    
    for sep in monday_separators:
        print(f"  Строка {sep[0]}, Время: {sep[3]}")
    
    # Показываем занятия между разделителями
    if len(monday_separators) >= 1:
        start_row = header_row + 1
        for i, sep in enumerate(monday_separators):
            end_row = sep[0]
            print(f"\nБлок {i+1} (строки {start_row}-{end_row-1}):")
            
            # Определяем четность блока
            if i == 0:
                week_parity = "НЕЧЕТНАЯ (первый блок после разделителя)"
            elif i == 1:
                week_parity = "ЧЕТНАЯ (второй блок после разделителя)"
            else:
                week_parity = f"Блок {i+1}"
            
            print(f"  Четность: {week_parity}")
            
            # Показываем занятия в блоке
            for row_idx in range(start_row, end_row):
                time_cell = parser.worksheet.cell(row=row_idx, column=2).value
                group_cell = parser.worksheet.cell(row=row_idx, column=group_col).value
                if group_cell and time_cell:
                    group_str = str(group_cell).strip()
                    if group_str and not (group_str.startswith('—') or group_str.startswith('─')):
                        time_str = str(time_cell).strip()
                        print(f"    Строка {row_idx}: {time_str} - {group_str[:50]}...")
            
            start_row = end_row + 1
        
        # Показываем занятия после последнего разделителя
        if len(monday_separators) > 0:
            last_sep_row = monday_separators[-1][0]
            print(f"\nБлок после последнего разделителя (строки {last_sep_row+1}-...):")
            week_parity = "ЧЕТНАЯ" if len(monday_separators) == 1 else "?"
            print(f"  Четность: {week_parity}")
        
except Exception as e:
    print(f"Ошибка: {e}")
    import traceback
    traceback.print_exc()

