"""Тест определения типа занятия"""
import sys
import io

# Исправление кодировки для Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# Импортируем метод из парсера
from excel_parser_v2 import ExcelScheduleParserV2

# Создаем экземпляр парсера для доступа к методам
parser = ExcelScheduleParserV2("dummy.xlsx")

test_cases = [
    "Интернет-маркетинг ст.пр.Кожухова Г.Н. 3-34",
    "МЕТРОЛОГИЯ, СТАНДАРТИЗАЦИЯ И СЕРТИФИКАЦИЯ (В ИНФОРМАЦИОННЫХ ТЕХНОЛОГИЯХ) доц.Авдашкова Л.П. 2-22",
    "Программирование в системе 1с:предприятие доц.Лебедева Е.В. 3-27",
    "ПРОГРАММИРОВАНИЕ В СИСТЕМЕ 1С:ПРЕДПРИЯТИЕ доц.Лебедева Е.В. 3-27",
    "Интеллектуальные информационные системы в экономике доц.Грибовская М.А. 3-27",
    "ИНТЕЛЛЕКТУАЛЬНЫЕ ИНФОРМАЦИОННЫЕ СИСТЕМЫ В ЭКОНОМИКЕ доц.Грибовская М.А. 3-27",
]

print("=" * 80)
print("ТЕСТ ОПРЕДЕЛЕНИЯ ТИПА ЗАНЯТИЯ")
print("=" * 80)

for test_case in test_cases:
    print(f"\nИсходная строка: {test_case}")
    
    # Извлекаем предмет
    subject = parser._extract_subject(test_case)
    print(f"  Извлеченный предмет: {subject}")
    
    # Определяем тип
    lesson_type = parser._detect_lesson_type(test_case)
    type_display = {'lecture': 'ЛЕКЦИЯ', 'practice': 'Практика', 'laboratory': 'Лабораторная'}.get(lesson_type, lesson_type)
    print(f"  Определенный тип: {type_display}")
    
    # Анализируем регистр
    uppercase_count = sum(1 for c in subject if c.isupper() and c.isalpha())
    lowercase_count = sum(1 for c in subject if c.islower() and c.isalpha())
    total_letters = uppercase_count + lowercase_count
    
    if total_letters > 0:
        uppercase_percentage = (uppercase_count / total_letters) * 100
        print(f"  Анализ: Заглавных: {uppercase_count}, Строчных: {lowercase_count}, Процент заглавных: {uppercase_percentage:.1f}%")
        
        if total_letters > 1:
            remaining_uppercase = max(0, uppercase_count - 1) if subject[0].isupper() else uppercase_count
            remaining_total = total_letters - 1
            if remaining_total > 0:
                remaining_uppercase_percentage = (remaining_uppercase / remaining_total) * 100
                print(f"  Оставшиеся буквы (без первой): Процент заглавных: {remaining_uppercase_percentage:.1f}%")

