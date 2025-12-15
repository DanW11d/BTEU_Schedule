"""Тест парсинга занятий"""
from excel_parser_v2 import ExcelScheduleParserV2

file_path = r"D:\Excel file\p-1 (28.08.25).xls"
parser = ExcelScheduleParserV2(file_path)
parser.load_file()

ws = parser.worksheet

# Находим столбец
group_col = parser._find_group_column("П-1")
print(f"Столбец группы: {group_col}")

# Находим заголовок
header_row = parser._find_header_row()
print(f"Строка заголовка: {header_row}")

# Показываем несколько строк после заголовка
print("\nСтроки после заголовка:")
for row_idx in range(header_row, min(header_row + 20, ws.max_row + 1)):
    day_cell = ws.cell(row=row_idx, column=1).value
    time_cell = ws.cell(row=row_idx, column=2).value
    group_cell = ws.cell(row=row_idx, column=group_col).value if group_col else None
    
    if day_cell or time_cell or group_cell:
        print(f"  Строка {row_idx}: день={repr(day_cell)}, время={repr(time_cell)}, занятие={repr(group_cell)}")

# Пробуем распарсить
print("\nПопытка парсинга:")
lessons = parser.parse("П-1", 1)
print(f"Найдено занятий: {len(lessons)}")
if lessons:
    print(f"Первое занятие: {lessons[0]}")

