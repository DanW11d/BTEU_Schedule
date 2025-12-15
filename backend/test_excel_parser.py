"""
Тестовый скрипт для проверки Excel парсера
Использование: python test_excel_parser.py <путь_к_файлу.xlsx> <код_группы>
"""
import sys
import os
from excel_parser import ExcelScheduleParser

def main():
    if len(sys.argv) < 3:
        print("Использование: python test_excel_parser.py <путь_к_файлу.xlsx> <код_группы> [group_id]")
        print("Пример: python test_excel_parser.py schedule.xlsx П-1")
        sys.exit(1)
    
    file_path = sys.argv[1]
    group_code = sys.argv[2]
    group_id = int(sys.argv[3]) if len(sys.argv) > 3 else 1
    
    if not os.path.exists(file_path):
        print(f"Ошибка: Файл {file_path} не найден!")
        sys.exit(1)
    
    print("=" * 60)
    print("Тестирование Excel парсера")
    print("=" * 60)
    print(f"Файл: {file_path}")
    print(f"Группа: {group_code} (ID: {group_id})")
    print("=" * 60)
    
    try:
        # Создаем парсер
        parser = ExcelScheduleParser(file_path)
        print("\n1. Загрузка файла...")
        parser.load_file()
        print(f"   ✓ Файл загружен. Лист: {parser.worksheet.title}")
        print(f"   Размер: {parser.worksheet.max_row} строк, {parser.worksheet.max_column} столбцов")
        
        # Находим начало и конец данных
        print("\n2. Определение структуры таблицы...")
        start_row = parser._find_data_start()
        end_row = parser._find_data_end()
        print(f"   ✓ Начало данных: строка {start_row}")
        print(f"   ✓ Конец данных: строка {end_row}")
        
        # Парсим
        print("\n3. Парсинг расписания...")
        lessons = parser.parse(group_code, group_id)
        print(f"   ✓ Найдено занятий: {len(lessons)}")
        
        # Валидируем
        print("\n4. Валидация данных...")
        is_valid, errors = parser.validate_lessons(lessons)
        if is_valid:
            print("   ✓ Все данные валидны")
        else:
            print(f"   ✗ Найдено ошибок: {len(errors)}")
            for error in errors[:10]:  # Показываем первые 10 ошибок
                print(f"     - {error}")
            if len(errors) > 10:
                print(f"     ... и еще {len(errors) - 10} ошибок")
        
        # Показываем примеры
        print("\n5. Примеры распарсенных занятий:")
        for i, lesson in enumerate(lessons[:5], 1):
            print(f"\n   Занятие {i}:")
            print(f"     День недели: {lesson['day_of_week']}")
            print(f"     Номер пары: {lesson['lesson_number']}")
            print(f"     Предмет: {lesson['subject']}")
            print(f"     Тип: {lesson['lesson_type']}")
            print(f"     Четность: {lesson['week_parity']}")
            if lesson.get('teacher'):
                print(f"     Преподаватель: {lesson['teacher']}")
            if lesson.get('classroom'):
                print(f"     Аудитория: {lesson['classroom']}")
        
        if len(lessons) > 5:
            print(f"\n   ... и еще {len(lessons) - 5} занятий")
        
        # Статистика
        print("\n6. Статистика:")
        odd_count = sum(1 for l in lessons if l['week_parity'] == 'odd')
        even_count = sum(1 for l in lessons if l['week_parity'] == 'even')
        both_count = sum(1 for l in lessons if l['week_parity'] == 'both')
        print(f"   Нечётная неделя: {odd_count}")
        print(f"   Чётная неделя: {even_count}")
        print(f"   Каждую неделю: {both_count}")
        
        lecture_count = sum(1 for l in lessons if l['lesson_type'] == 'lecture')
        practice_count = sum(1 for l in lessons if l['lesson_type'] == 'practice')
        lab_count = sum(1 for l in lessons if l['lesson_type'] == 'laboratory')
        print(f"   Лекции: {lecture_count}")
        print(f"   Практики: {practice_count}")
        print(f"   Лабораторные: {lab_count}")
        
        print("\n" + "=" * 60)
        print("Парсинг завершен успешно!")
        print("=" * 60)
        
    except Exception as e:
        print(f"\n✗ Ошибка: {str(e)}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

if __name__ == '__main__':
    main()
