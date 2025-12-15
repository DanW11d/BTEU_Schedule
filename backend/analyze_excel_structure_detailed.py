"""Детальный анализ структуры Excel файла для понимания логики определения пар"""
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
print("ДЕТАЛЬНЫЙ АНАЛИЗ СТРУКТУРЫ EXCEL ФАЙЛА")
print("=" * 80)

try:
    parser = ExcelScheduleParserV2(filepath)
    parser.load_file()
    
    group_col = parser._find_group_column('S-4')
    header_row = parser._find_header_row()
    
    print(f"\nСтолбец группы: {group_col}")
    print(f"Строка заголовка: {header_row}\n")
    
    # Анализируем структуру понедельника детально
    print("=" * 80)
    print("ДЕТАЛЬНАЯ СТРУКТУРА ПОНЕДЕЛЬНИКА")
    print("=" * 80)
    
    current_day = None
    pair_number_by_time = {}  # Словарь для сопоставления времени и номера пары
    
    # Определяем соответствие времени и номера пары
    time_to_pair = {
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
    
    print("\nСтрока | День | Время | Номер пары (по времени) | Содержимое группы")
    print("-" * 80)
    
    for row_idx in range(header_row + 1, min(header_row + 50, parser.worksheet.max_row + 1)):
        day_cell = parser.worksheet.cell(row=row_idx, column=1).value
        time_cell = parser.worksheet.cell(row=row_idx, column=2).value
        group_cell = parser.worksheet.cell(row=row_idx, column=group_col).value
        
        day_str = str(day_cell).strip().lower() if day_cell else ""
        time_str = str(time_cell).strip() if time_cell else ""
        group_str = str(group_cell).strip()[:40] if group_cell else ""
        
        # Определяем день
        if day_cell:
            day_str_lower = day_str.lower()
            if 'понедель' in day_str_lower or 'пн' in day_str_lower:
                if current_day != 1:
                    current_day = 1
                    print(f"\n{'='*80}")
                    print("ПОНЕДЕЛЬНИК")
                    print(f"{'='*80}\n")
            elif 'вторник' in day_str_lower or 'вт' in day_str_lower:
                if current_day == 1:
                    break
        
        if current_day == 1:
            # Определяем номер пары по времени
            pair_num_by_time = None
            for time_pattern, pair in time_to_pair.items():
                if time_pattern in time_str:
                    pair_num_by_time = pair
                    break
            
            # Проверяем, является ли это разделителем
            is_separator = False
            if group_str:
                clean_str = group_str.replace(' ', '').replace('─', '').replace('—', '').replace('-', '')
                if (group_str.startswith('—') or group_str.startswith('─') or 
                    (len(clean_str) < 3 and ('—' in group_str or '─' in group_str))):
                    is_separator = True
            
            day_display = day_str[:10] if day_str else ""
            time_display = time_str[:15] if time_str else ""
            pair_display = str(pair_num_by_time) if pair_num_by_time else "-"
            group_display = "[РАЗДЕЛИТЕЛЬ]" if is_separator else group_str[:30]
            
            print(f"{row_idx:6d} | {day_display:10s} | {time_display:15s} | {pair_display:20s} | {group_display}")
    
    print("\n" + "=" * 80)
    print("АНАЛИЗ ЛОГИКИ ОПРЕДЕЛЕНИЯ НОМЕРОВ ПАР")
    print("=" * 80)
    print("\nПРОБЛЕМА: В текущем парсере номер пары определяется инкрементально,")
    print("а должен определяться по времени занятия!")
    print("\nПРАВИЛЬНАЯ ЛОГИКА:")
    print("1. Номер пары определяется по времени начала занятия")
    print("2. Если время 9:00-9:45 или 9:50-10:35 → Пара 1")
    print("3. Если время 10:50-11:35 или 11:40-12:25 → Пара 2")
    print("4. Если время 12:55-13:40 или 13:45-14:30 → Пара 3")
    print("5. Если время 14:40-15:25 или 15:30-16:15 → Пара 4")
    print("6. Если время 16:25-17:10 или 17:15-18:00 → Пара 5")
    print("\nНужно изменить парсер, чтобы он определял номер пары по времени, а не инкрементально!")
    
except Exception as e:
    print(f"❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()
