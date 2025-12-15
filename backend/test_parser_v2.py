"""Тестирование нового парсера"""
import sys
import io

if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

from excel_parser_v2 import ExcelScheduleParserV2

# Тестируем на файле p-1
file_path = r"D:\Excel file\p-1 (28.08.25).xls"
group_code = "П-1"
group_id = 30

print(f"Тестирование парсера на файле: {file_path}")
print(f"Группа: {group_code} (ID: {group_id})")
print("=" * 70)

try:
    parser = ExcelScheduleParserV2(file_path)
    lessons = parser.parse(group_code, group_id)
    
    print(f"\nНайдено занятий: {len(lessons)}")
    print()
    
    if lessons:
        print("Первые 5 занятий:")
        for i, lesson in enumerate(lessons[:5], 1):
            print(f"{i}. День: {lesson['day_of_week']}, Пара: {lesson['lesson_number']}")
            print(f"   Предмет: {lesson['subject'][:50]}")
            print(f"   Преподаватель: {lesson['teacher']}")
            print(f"   Аудитория: {lesson['classroom']}")
            print(f"   Неделя: {lesson['week_parity']}")
            print()
    else:
        print("Занятия не найдены. Возможные причины:")
        print("1. Группа не найдена в файле (в файле могут быть другие группы)")
        print("2. Формат файла отличается от ожидаемого")
        print("3. Данные в другом формате")
        
        # Проверяем, какие группы есть в файле
        parser.load_file()
        header_row = parser._find_header_row()
        if header_row:
            print(f"\nНайден заголовок в строке {header_row}:")
            for col in range(1, min(10, parser.worksheet.max_column + 1)):
                cell = parser.worksheet.cell(row=header_row, column=col)
                if cell.value:
                    print(f"  Столбец {col}: {cell.value}")
        
except Exception as e:
    print(f"Ошибка: {e}")
    import traceback
    traceback.print_exc()

