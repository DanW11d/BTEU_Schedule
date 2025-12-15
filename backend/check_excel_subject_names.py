"""Проверка названий предметов в Excel файле"""
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

print("=" * 80)
print("ПРОВЕРКА НАЗВАНИЙ ПРЕДМЕТОВ В EXCEL ФАЙЛЕ")
print("=" * 80)

try:
    parser = ExcelScheduleParserV2(filepath)
    parser.load_file()
    
    group_col = parser._find_group_column('S-4')
    header_row = parser._find_header_row()
    
    print(f"\nСтолбец группы: {group_col}")
    print(f"Строка заголовка: {header_row}\n")
    
    # Ищем "Интернет-маркетинг" в файле
    print("Поиск 'Интернет-маркетинг' в Excel файле:\n")
    
    found_count = 0
    for row_idx in range(header_row + 1, min(header_row + 100, parser.worksheet.max_row + 1)):
        group_cell = parser.worksheet.cell(row=row_idx, column=group_col).value
        
        if group_cell:
            cell_str = str(group_cell).strip()
            
            if 'интернет' in cell_str.lower() and 'маркетинг' in cell_str.lower():
                found_count += 1
                print(f"Строка {row_idx}:")
                print(f"  Исходное содержимое: {cell_str}")
                
                # Извлекаем предмет
                subject = parser._extract_subject(cell_str)
                print(f"  Извлеченный предмет: {subject}")
                
                # Определяем тип
                lesson_type = parser._detect_lesson_type(cell_str)
                type_display = {'lecture': 'ЛЕКЦИЯ', 'practice': 'Практика'}.get(lesson_type, lesson_type)
                print(f"  Определенный тип: {type_display}")
                
                # Анализируем регистр исходной строки
                uppercase_in_original = sum(1 for c in cell_str if c.isupper() and c.isalpha())
                lowercase_in_original = sum(1 for c in cell_str if c.islower() and c.isalpha())
                total_in_original = uppercase_in_original + lowercase_in_original
                
                if total_in_original > 0:
                    print(f"  Регистр в исходной строке: Заглавных: {uppercase_in_original}, Строчных: {lowercase_in_original}")
                
                # Анализируем регистр извлеченного предмета
                uppercase_in_subject = sum(1 for c in subject if c.isupper() and c.isalpha())
                lowercase_in_subject = sum(1 for c in subject if c.islower() and c.isalpha())
                total_in_subject = uppercase_in_subject + lowercase_in_subject
                
                if total_in_subject > 0:
                    print(f"  Регистр в извлеченном предмете: Заглавных: {uppercase_in_subject}, Строчных: {lowercase_in_subject}")
                
                print()
    
    print(f"Всего найдено: {found_count}")
    
except Exception as e:
    print(f"❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

