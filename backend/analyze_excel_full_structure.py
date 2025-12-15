"""Полный анализ структуры Excel файла для понимания четности недели"""
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
print("ПОЛНЫЙ АНАЛИЗ СТРУКТУРЫ EXCEL ФАЙЛА")
print("=" * 60)

try:
    parser = ExcelScheduleParserV2(filepath)
    parser.load_file()
    
    group_col = parser._find_group_column('S-4')
    header_row = parser._find_header_row()
    
    print(f"\nСтолбец группы: {group_col}")
    print(f"Строка заголовка: {header_row}\n")
    
    # Анализируем структуру понедельника полностью
    print("=" * 60)
    print("ПОЛНАЯ СТРУКТУРА ПОНЕДЕЛЬНИКА")
    print("=" * 60)
    
    current_day = None
    separator_count = 0
    
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
                    print(f"\n{'='*60}")
                    print(f"Строка {row_idx}: НАЙДЕН ПОНЕДЕЛЬНИК")
                    print(f"{'='*60}")
                    separator_count = 0
                elif current_day != 1:
                    # Перешли на другой день
                    break
        
        if current_day == 1:
            time_str = str(time_cell).strip() if time_cell else ""
            group_str = str(group_cell).strip() if group_cell else ""
            
            # Проверяем на разделитель
            is_separator = False
            if group_str:
                clean_str = group_str.replace(' ', '').replace('─', '').replace('—', '').replace('-', '')
                if (group_str.startswith('—') or group_str.startswith('─') or 
                    (len(clean_str) < 3 and ('—' in group_str or '─' in group_str or group_str.count('-') > 5))):
                    is_separator = True
                    separator_count += 1
            
            print(f"\nСтрока {row_idx}:")
            print(f"  День: '{day_cell}'")
            print(f"  Время: '{time_str}'")
            print(f"  Группа: '{group_str[:80]}...'")
            
            if is_separator:
                print(f"  ⚠ РАЗДЕЛИТЕЛЬ НЕДЕЛЬ #{separator_count}")
                if separator_count == 1:
                    print(f"     → После этого разделителя: НЕЧЕТНАЯ неделя")
                elif separator_count == 2:
                    print(f"     → После этого разделителя: ЧЕТНАЯ неделя")
                elif separator_count == 3:
                    print(f"     → После этого разделителя: НЕЧЕТНАЯ неделя")
            else:
                # Определяем четность по разделителям
                if separator_count == 0:
                    week_parity = "НЕ ОПРЕДЕЛЕНО (до первого разделителя)"
                elif separator_count == 1:
                    week_parity = "НЕЧЕТНАЯ"
                elif separator_count == 2:
                    week_parity = "ЧЕТНАЯ"
                elif separator_count == 3:
                    week_parity = "НЕЧЕТНАЯ"
                else:
                    week_parity = f"ЧЕРЕЗ {separator_count} разделителей"
                
                print(f"  Четность (по разделителям): {week_parity}")
                
                # Ищем номера недель в содержимом
                import re
                week_nums = re.findall(r'\d+', group_str)
                if week_nums and len(week_nums) >= 2:
                    print(f"  Диапазон недель в содержимом: {week_nums[0]}-{week_nums[1]}")
        
except Exception as e:
    print(f"Ошибка: {e}")
    import traceback
    traceback.print_exc()

