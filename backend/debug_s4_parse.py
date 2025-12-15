"""Отладка парсинга файлов S-4"""
import os
import sys
import io
from excel_parser_v2 import ExcelScheduleParserV2

# Исправление кодировки для Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

EXCEL_DIR = r"D:\Excel file"

def debug_parse():
    """Отлаживает парсинг файлов S-4"""
    print("=" * 60)
    print("ОТЛАДКА ПАРСИНГА ФАЙЛОВ S-4")
    print("=" * 60)
    
    files = [f for f in os.listdir(EXCEL_DIR) 
             if f.lower().startswith('s-4') and (f.endswith('.xls') or f.endswith('.xlsx'))]
    
    if not files:
        print("❌ Файлы S-4 не найдены!")
        return
    
    for filename in files[:1]:  # Берем первый файл для отладки
        filepath = os.path.join(EXCEL_DIR, filename)
        print(f"\nФайл: {filename}")
        print("-" * 60)
        
        try:
            parser = ExcelScheduleParserV2(filepath)
            parser.load_file()
            
            # Проверяем, находит ли парсер группу
            group_col = parser._find_group_column('S-4')
            if group_col:
                print(f"✅ Столбец группы S-4 найден: {group_col}")
            else:
                print("❌ Столбец группы S-4 не найден!")
                # Показываем первые строки для анализа
                print("\nПервые 10 строк файла:")
                for row in range(1, min(11, parser.worksheet.max_row + 1)):
                    row_data = []
                    for col in range(1, min(11, parser.worksheet.max_column + 1)):
                        cell = parser.worksheet.cell(row=row, column=col)
                        if cell.value:
                            row_data.append(str(cell.value)[:30])
                    if row_data:
                        print(f"  Строка {row}: {' | '.join(row_data)}")
                continue
            
            # Парсим
            lessons = parser.parse('S-4', 51)  # group_id = 51 для S-4
            print(f"\n✅ Найдено занятий: {len(lessons)}")
            
            if lessons:
                print("\nПервые 5 занятий:")
                for i, lesson in enumerate(lessons[:5], 1):
                    print(f"\n  {i}. Пара {lesson['lesson_number']}, День {lesson['day_of_week']}")
                    print(f"     Предмет: {lesson['subject'][:60]}")
                    print(f"     Преподаватель: {lesson['teacher']}")
                    print(f"     Аудитория: {lesson['classroom']}")
            else:
                print("\n❌ Занятия не найдены!")
                # Показываем структуру файла
                header_row = parser._find_header_row()
                if header_row:
                    print(f"\nСтрока заголовка: {header_row}")
                    print("Данные вокруг строки заголовка:")
                    for row in range(max(1, header_row - 2), min(header_row + 5, parser.worksheet.max_row + 1)):
                        group_cell = parser.worksheet.cell(row=row, column=group_col)
                        day_cell = parser.worksheet.cell(row=row, column=1)
                        time_cell = parser.worksheet.cell(row=row, column=2)
                        if group_cell.value or day_cell.value or time_cell.value:
                            print(f"  Строка {row}: день='{day_cell.value}', время='{time_cell.value}', группа='{group_cell.value}'")
                
        except Exception as e:
            print(f"❌ Ошибка: {e}")
            import traceback
            traceback.print_exc()

if __name__ == "__main__":
    debug_parse()

