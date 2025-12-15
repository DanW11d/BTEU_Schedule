"""Сравнение структуры двух файлов"""
import os
import sys
import io
from excel_parser_v2 import ExcelScheduleParserV2

# Исправление кодировки для Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

EXCEL_DIR = r"D:\Excel file"

files = [
    "s-4 (28.08.25).xls",
    "s-4-s-15.09.25 (12.09.25).xls"
]

print("=" * 60)
print("СРАВНЕНИЕ СТРУКТУРЫ ФАЙЛОВ")
print("=" * 60)

for filename in files:
    filepath = os.path.join(EXCEL_DIR, filename)
    
    if not os.path.exists(filepath):
        print(f"\n❌ Файл {filename} не найден!")
        continue
    
    print(f"\n{'='*60}")
    print(f"ФАЙЛ: {filename}")
    print(f"{'='*60}")
    
    try:
        parser = ExcelScheduleParserV2(filepath)
        parser.load_file()
        
        group_col = parser._find_group_column('S-4')
        header_row = parser._find_header_row()
        
        # Считаем разделители в понедельнике
        separator_count = 0
        current_day = None
        
        for row_idx in range(header_row + 1, min(header_row + 50, parser.worksheet.max_row + 1)):
            day_cell = parser.worksheet.cell(row=row_idx, column=1).value
            group_cell = parser.worksheet.cell(row=row_idx, column=group_col).value
            
            if day_cell:
                day_str = str(day_cell).strip().lower()
                if day_str in parser.DAYS_OF_WEEK:
                    current_day = parser.DAYS_OF_WEEK[day_str]
                    if current_day == 1:  # Понедельник
                        separator_count = 0
            
            if current_day == 1 and group_cell:
                group_str = str(group_cell).strip()
                clean_str = group_str.replace(' ', '').replace('─', '').replace('—', '').replace('-', '')
                
                is_separator = False
                if group_str:
                    if (group_str.startswith('—') or group_str.startswith('─') or 
                        (len(clean_str) < 3 and ('—' in group_str or '─' in group_str or group_str.count('-') > 5))):
                        is_separator = True
                
                if is_separator:
                    separator_count += 1
            
            if day_cell and current_day and current_day != 1:
                break
        
        print(f"\nРазделителей в понедельнике: {separator_count}")
        
        if separator_count == 0:
            print("  → НЕТ РАЗДЕЛЕНИЯ: все занятия должны быть нечетной недели")
        elif separator_count == 1:
            print("  → ЕСТЬ РАЗДЕЛЕНИЕ: после разделителя идет нечетная неделя")
        else:
            print(f"  → ЕСТЬ РАЗДЕЛЕНИЕ: {separator_count} разделителей, чередуются недели")
        
        # Парсим и проверяем результат
        lessons = parser.parse(group_id=51, group_code='S-4')
        monday_lessons = [l for l in lessons if l.get('day_of_week') == 1]
        
        odd_count = len([l for l in monday_lessons if l.get('week_parity') == 'odd'])
        even_count = len([l for l in monday_lessons if l.get('week_parity') == 'even'])
        
        print(f"\nРезультат парсинга понедельника:")
        print(f"  Нечетная: {odd_count} занятий")
        print(f"  Четная: {even_count} занятий")
        
        if separator_count == 0 and even_count > 0:
            print(f"  ⚠ ПРОБЛЕМА: Нет разделителей, но есть занятия четной недели!")
        elif separator_count > 0 and even_count == 0:
            print(f"  ⚠ ПРОБЛЕМА: Есть разделители, но нет занятий четной недели!")
        
    except Exception as e:
        print(f"❌ Ошибка: {e}")
        import traceback
        traceback.print_exc()

