"""
Скрипт для отладки парсера - показывает структуру Excel файла
"""
import sys
import os
from pathlib import Path
import io

if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def debug_file(file_path: str, max_rows: int = 50):
    """Показывает структуру Excel файла"""
    from excel_parser import ExcelScheduleParser
    
    print("=" * 70)
    print(f"ОТЛАДКА ФАЙЛА: {os.path.basename(file_path)}")
    print("=" * 70)
    print()
    
    try:
        parser = ExcelScheduleParser(file_path)
        parser.load_file()
        
        print(f"Максимальная строка: {parser.worksheet.max_row}")
        print(f"Максимальная колонка: {parser.worksheet.max_column}")
        print()
        print("Первые строки файла:")
        print("-" * 70)
        
        for row_idx in range(1, min(max_rows + 1, parser.worksheet.max_row + 1)):
            row_data = []
            for col_idx in range(1, min(10, parser.worksheet.max_column + 1)):
                cell = parser.worksheet.cell(row=row_idx, column=col_idx)
                value = cell.value
                if value is not None:
                    row_data.append(str(value)[:30])  # Ограничиваем длину
                else:
                    row_data.append("")
            
            if any(row_data):  # Показываем только непустые строки
                print(f"Строка {row_idx:3d}: {' | '.join(f'{v:30s}' for v in row_data)}")
        
        print()
        print("=" * 70)
        print("ПОПЫТКА ПАРСИНГА:")
        print("=" * 70)
        
        # Пробуем распарсить
        lessons = parser.parse("TEST", 1)
        print(f"Найдено занятий: {len(lessons)}")
        
        if lessons:
            print("\nПервые занятия:")
            for i, lesson in enumerate(lessons[:5], 1):
                print(f"  {i}. {lesson}")
        else:
            print("\n⚠ Занятия не найдены!")
            print("\nПроверка разделителей недель:")
            week_separator = "─────────"
            found_separators = []
            for row_idx in range(1, min(100, parser.worksheet.max_row + 1)):
                for col_idx in range(1, min(10, parser.worksheet.max_column + 1)):
                    cell = parser.worksheet.cell(row=row_idx, column=col_idx)
                    if cell.value and week_separator in str(cell.value):
                        found_separators.append((row_idx, col_idx, str(cell.value)[:50]))
            
            if found_separators:
                print(f"  Найдено разделителей: {len(found_separators)}")
                for row, col, value in found_separators[:3]:
                    print(f"    Строка {row}, колонка {col}: {value}")
            else:
                print("  Разделители не найдены!")
        
    except Exception as e:
        print(f"❌ Ошибка: {e}")
        import traceback
        traceback.print_exc()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Использование: python debug_parser.py <путь_к_файлу>")
        print("\nПример:")
        print('  python backend/debug_parser.py "D:\\Excel file\\p-2 (28.08.25).xlsx"')
        sys.exit(1)
    
    file_path = sys.argv[1]
    if not os.path.exists(file_path):
        print(f"❌ Файл не найден: {file_path}")
        sys.exit(1)
    
    debug_file(file_path)

