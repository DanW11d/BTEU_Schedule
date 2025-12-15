"""
Скрипт для анализа структуры Excel файла
Помогает понять формат файла и почему парсер не находит данные
"""
import sys
import os
from pathlib import Path
from excel_parser import ExcelScheduleParser

def analyze_file(file_path: str):
    """Анализирует структуру Excel файла"""
    print("=" * 70)
    print(f"АНАЛИЗ ФАЙЛА: {os.path.basename(file_path)}")
    print("=" * 70)
    print()
    
    try:
        parser = ExcelScheduleParser(file_path)
        parser.load_file()
        
        ws = parser.worksheet
        print(f"Размеры файла: {ws.max_row} строк, {ws.max_column} столбцов")
        print()
        
        # Показываем первые 30 строк
        print("Первые 30 строк файла:")
        print("-" * 70)
        for row_idx in range(1, min(31, ws.max_row + 1)):
            row_data = []
            for col_idx in range(1, min(8, ws.max_column + 1)):
                cell = ws.cell(row=row_idx, column=col_idx)
                value = cell.value
                if value is None:
                    value = ""
                else:
                    value = str(value).strip()[:30]  # Ограничиваем длину
                row_data.append(value)
            
            # Показываем только непустые строки
            if any(row_data):
                print(f"Строка {row_idx:3d}: {' | '.join(f'{v:30s}' for v in row_data)}")
        
        print()
        print("-" * 70)
        
        # Проверяем формат
        is_column = parser._is_column_format()
        print(f"Формат: {'СТОЛБЦОВЫЙ' if is_column else 'ПОСТРОЧНЫЙ'}")
        print()
        
        # Пробуем найти разделители недель
        print("Поиск разделителей недель:")
        week_separators_found = []
        for row_idx in range(1, min(50, ws.max_row + 1)):
            row = list(ws.iter_rows(min_row=row_idx, max_row=row_idx, values_only=False))
            if row and row[0]:
                row_cells = row[0]
                week_parity = parser._detect_week_separator(row_cells)
                if week_parity:
                    week_separators_found.append((row_idx, week_parity))
                    row_text = " ".join([str(cell.value or "") for cell in row_cells[:5]])
                    print(f"  Строка {row_idx}: {week_parity} - {row_text[:60]}")
        
        if not week_separators_found:
            print("  Разделители недель не найдены!")
        
        print()
        
        # Пробуем найти начало данных
        start_row = parser._find_data_start()
        end_row = parser._find_data_end()
        print(f"Начало данных: строка {start_row}")
        print(f"Конец данных: строка {end_row}")
        print()
        
        # Пробуем распарсить
        print("Попытка парсинга (с тестовыми данными):")
        try:
            lessons = parser.parse("TEST", 1)
            print(f"  Найдено занятий: {len(lessons)}")
            if lessons:
                print(f"  Первое занятие: {lessons[0]}")
        except Exception as e:
            print(f"  Ошибка парсинга: {e}")
        
    except Exception as e:
        print(f"Ошибка анализа: {e}")
        import traceback
        traceback.print_exc()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Использование: python debug_file_structure.py <путь_к_файлу>")
        print()
        print("Пример:")
        print('  python backend/debug_file_structure.py "D:\\Excel file\\p-1 (28.08.25).xls"')
        sys.exit(1)
    
    file_path = sys.argv[1]
    if not os.path.exists(file_path):
        print(f"Файл не найден: {file_path}")
        sys.exit(1)
    
    analyze_file(file_path)

