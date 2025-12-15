"""Тест поиска группы в файле"""
from excel_parser_v2 import ExcelScheduleParserV2

file_path = r"D:\Excel file\p-1 (28.08.25).xls"
parser = ExcelScheduleParserV2(file_path)
parser.load_file()

ws = parser.worksheet

print("Строка 12 (заголовки):")
for col in range(1, 6):
    cell = ws.cell(row=12, column=col)
    value = cell.value
    print(f"  Колонка {col}: {repr(value)}")

print("\nПоиск группы П-1:")
col = parser._find_group_column("П-1")
print(f"  Столбец: {col}")

print("\nПоиск группы П-11:")
col = parser._find_group_column("П-11")
print(f"  Столбец: {col}")

print("\nПроверка совпадения:")
cell_value = ws.cell(row=12, column=3).value
print(f"  Ячейка (12,3): {repr(cell_value)}")
if cell_value:
    matches = parser._matches_group_code(str(cell_value), "П-11")
    print(f"  Совпадает с П-11: {matches}")

