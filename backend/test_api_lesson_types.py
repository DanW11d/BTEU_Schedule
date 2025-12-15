"""Проверка типов занятий через API"""
import requests
import json
import sys
import io

# Исправление кодировки для Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

API_URL = "http://localhost:8000/v1/schedule/week"

print("=" * 80)
print("ПРОВЕРКА ТИПОВ ЗАНЯТИЙ ЧЕРЕЗ API")
print("=" * 80)

try:
    # Получаем расписание для группы S-4
    params = {
        'group': 'S-4',
        'week': 'odd'
    }
    
    response = requests.get(API_URL, params=params)
    
    if response.status_code == 200:
        lessons = response.json()
        
        print(f"\nПолучено занятий: {len(lessons)}\n")
        
        # Ищем "Интернет-маркетинг"
        internet_marketing = [l for l in lessons if 'интернет' in l.get('subject', '').lower() and 'маркетинг' in l.get('subject', '').lower()]
        
        print(f"Найдено занятий 'Интернет-маркетинг': {len(internet_marketing)}\n")
        
        for lesson in internet_marketing:
            subject = lesson.get('subject', '')
            lesson_type = lesson.get('lessonType', '')
            day = lesson.get('dayOfWeek', 0)
            pair = lesson.get('lessonNumber', 0)
            
            days = ['ПН', 'ВТ', 'СР', 'ЧТ', 'ПТ', 'СБ']
            day_name = days[day - 1] if 1 <= day <= 6 else f"День {day}"
            
            type_display = {'lecture': 'ЛЕКЦИЯ', 'practice': 'Практика', 'laboratory': 'Лабораторная'}.get(lesson_type, lesson_type)
            
            print(f"Предмет: {subject}")
            print(f"  Тип: {type_display} (в API: {lesson_type})")
            print(f"  День: {day_name}, Пара: {pair}")
            print()
        
        # Показываем все типы занятий
        print("=" * 80)
        print("ВСЕ ТИПЫ ЗАНЯТИЙ В API")
        print("=" * 80)
        
        type_counts = {}
        for lesson in lessons:
            lesson_type = lesson.get('lessonType', 'unknown')
            type_counts[lesson_type] = type_counts.get(lesson_type, 0) + 1
        
        for lesson_type, count in sorted(type_counts.items()):
            type_display = {'lecture': 'ЛЕКЦИЯ', 'practice': 'Практика', 'laboratory': 'Лабораторная'}.get(lesson_type, lesson_type)
            print(f"{type_display:15s}: {count} занятий")
        
    else:
        print(f"❌ Ошибка API: {response.status_code}")
        print(response.text)
        
except Exception as e:
    print(f"❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

