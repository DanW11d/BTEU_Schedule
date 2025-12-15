"""Анализ логики определения номеров пар в Excel файле"""
import os
import sys
import io
from openpyxl import load_workbook
from xlrd import open_workbook
import xlrd

# Исправление кодировки для Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

EXCEL_DIR = r"D:\Excel file"
filename = "s-4 (28.08.25).xls"
filepath = os.path.join(EXCEL_DIR, filename)

print("=" * 80)
print("АНАЛИЗ ЛОГИКИ ОПРЕДЕЛЕНИЯ НОМЕРОВ ПАР")
print("=" * 80)

try:
    # Загружаем файл
    if filename.endswith('.xls'):
        # Используем xlrd для .xls файлов
        wb = xlrd.open_workbook(filepath)
        sheet = wb.sheet_by_index(0)
        
        print(f"\nФайл: {filename}")
        print(f"Строк: {sheet.nrows}, Столбцов: {sheet.ncols}\n")
        
        # Ищем столбец группы S-4 (обычно это столбец 3, индекс 2)
        group_col_idx = 2  # Столбец C (индекс 2)
        
        # Ищем строку заголовка (где указаны группы)
        header_row = None
        for row_idx in range(min(20, sheet.nrows)):
            try:
                cell_value = sheet.cell_value(row_idx, group_col_idx)
                if 's-4' in str(cell_value).lower() or 'S-4' in str(cell_value):
                    header_row = row_idx
                    break
            except:
                pass
        
        if header_row is None:
            print("❌ Не найдена строка заголовка!")
            exit(1)
        
        print(f"Строка заголовка: {header_row + 1} (индекс {header_row})\n")
        
        # Анализируем структуру понедельника
        print("=" * 80)
        print("СТРУКТУРА ПОНЕДЕЛЬНИКА")
        print("=" * 80)
        
        current_day = None
        day_names = {
            'понедель': 1, 'пн': 1,
            'вторник': 2, 'вт': 2,
            'среда': 3, 'ср': 3,
            'четверг': 4, 'чт': 4,
            'пятница': 5, 'пт': 5,
            'суббота': 6, 'сб': 6
        }
        
        pair_times = {
            '9:00-9:45': 1, '9.00-9.45': 1,
            '9:50-10:35': 1, '9.50-10.35': 1,
            '10:50-11:35': 2, '10.50-11.35': 2,
            '11:40-12:25': 2, '11.40-12.25': 2,
            '12:55-13:40': 3, '12.55-13.40': 3,
            '13:45-14:30': 3, '13.45-14.30': 3,
            '14:40-15:25': 4, '14.40-15.25': 4,
            '15:30-16:15': 4, '15.30-16.15': 4,
            '16:25-17:10': 5, '16.25-17.10': 5,
            '17:15-18:00': 5, '17.15-18.00': 5,
        }
        
        print("\nДетальная структура строк:\n")
        
        for row_idx in range(header_row + 1, min(header_row + 50, sheet.nrows)):
            try:
                day_cell = sheet.cell_value(row_idx, 0)  # Столбец A
                time_cell = sheet.cell_value(row_idx, 1)  # Столбец B
                group_cell = sheet.cell_value(row_idx, group_col_idx)  # Столбец группы
                
                day_str = str(day_cell).strip().lower() if day_cell else ""
                time_str = str(time_cell).strip() if time_cell else ""
                group_str = str(group_cell).strip() if group_cell else ""
                
                # Определяем день
                if day_cell:
                    for day_key, day_num in day_names.items():
                        if day_key in day_str:
                            if current_day != day_num:
                                current_day = day_num
                                if day_num == 1:  # Понедельник
                                    print(f"\n{'='*80}")
                                    print(f"ПОНЕДЕЛЬНИК (строка {row_idx + 1})")
                                    print(f"{'='*80}\n")
                            break
                
                if current_day == 1:  # Анализируем только понедельник
                    # Определяем номер пары по времени
                    pair_num = None
                    for time_pattern, pair in pair_times.items():
                        if time_pattern in time_str:
                            pair_num = pair
                            break
                    
                    # Проверяем, является ли это разделителем
                    is_separator = False
                    if group_str:
                        clean_str = group_str.replace(' ', '').replace('─', '').replace('—', '').replace('-', '')
                        if (group_str.startswith('—') or group_str.startswith('─') or 
                            (len(clean_str) < 3 and ('—' in group_str or '─' in group_str))):
                            is_separator = True
                    
                    if is_separator:
                        print(f"Строка {row_idx + 1:3d}: [РАЗДЕЛИТЕЛЬ]")
                    elif time_str and pair_num:
                        print(f"Строка {row_idx + 1:3d}: Время: {time_str:15s} → Пара {pair_num}", end="")
                        if group_str and not is_separator:
                            subject_preview = group_str[:50].replace('\n', ' ')
                            print(f" | {subject_preview}...")
                        else:
                            print()
                    elif group_str and not is_separator:
                        print(f"Строка {row_idx + 1:3d}: [ПРОДОЛЖЕНИЕ] {group_str[:50]}...")
                
                # Переходим к следующему дню
                if day_cell and current_day and current_day != 1:
                    break
                    
            except Exception as e:
                continue
        
        print("\n" + "=" * 80)
        print("ВЫВОДЫ")
        print("=" * 80)
        print("\nЛогика определения номеров пар:")
        print("1. Номер пары определяется по времени начала занятия")
        print("2. Пара 1: 9:00-9:45 и 9:50-10:35")
        print("3. Пара 2: 10:50-11:35 и 11:40-12:25")
        print("4. Пара 3: 12:55-13:40 и 13:45-14:30")
        print("5. Пара 4: 14:40-15:25 и 15:30-16:15")
        print("6. Пара 5: 16:25-17:10 и 17:15-18:00")
        print("\nЕсли время не найдено, номер пары должен определяться по предыдущему времени")
        
except Exception as e:
    print(f"❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

