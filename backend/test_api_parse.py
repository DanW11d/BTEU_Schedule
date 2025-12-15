"""Тест парсинга через основной парсер"""
from excel_parser import ExcelScheduleParser

file_path = r"D:\Excel file\p-1 (28.08.25).xls"
parser = ExcelScheduleParser(file_path)
parser.load_file()

is_col = parser._is_column_format()
print(f"Столбцовый формат: {is_col}")

lessons = parser.parse("П-1", 30)
print(f"Найдено занятий: {len(lessons)}")
if lessons:
    print(f"Первое занятие: {lessons[0]}")

