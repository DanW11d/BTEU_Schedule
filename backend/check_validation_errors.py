"""Проверка ошибок валидации для одного файла"""
import sys
import io
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

from excel_parser import ExcelScheduleParser

if len(sys.argv) < 3:
    print("Использование: python check_validation_errors.py <файл> <код_группы> <group_id>")
    print('Пример: python backend/check_validation_errors.py "D:\\Excel file\\a-1 (28.08.25).xls" "А-1" 26')
    sys.exit(1)

file_path = sys.argv[1]
group_code = sys.argv[2]
group_id = int(sys.argv[3])

print(f"Проверка файла: {file_path}")
print(f"Группа: {group_code} (ID: {group_id})")
print("=" * 70)

try:
    parser = ExcelScheduleParser(file_path)
    lessons = parser.parse(group_code, group_id)
    
    print(f"\nНайдено занятий: {len(lessons)}")
    
    if lessons:
        print(f"\nПервое занятие:")
        first = lessons[0]
        for key, value in first.items():
            print(f"  {key}: {value}")
    
    is_valid, errors = parser.validate_lessons(lessons)
    
    print(f"\nВалидация: {'[OK] ПРОЙДЕНА' if is_valid else '[ERROR] ОШИБКИ'}")
    
    if errors:
        print(f"\nОшибки валидации ({len(errors)}):")
        for i, error in enumerate(errors[:20], 1):  # Показываем первые 20
            print(f"  {i}. {error}")
        if len(errors) > 20:
            print(f"  ... и еще {len(errors) - 20} ошибок")
    else:
        print("\nОшибок нет!")
        
except Exception as e:
    print(f"Ошибка: {e}")
    import traceback
    traceback.print_exc()

