"""Тест API для получения расписания"""
import requests
import sys
import io

# Исправление кодировки для Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

API_URL = "http://localhost:8000/v1"

# Тест получения расписания на понедельник для четной недели
group_code = "S-4"
day = 1  # Понедельник
week = "even"  # Четная неделя

url = f"{API_URL}/schedule/group/{group_code}/day/{day}?week={week}"

print("=" * 60)
print("ТЕСТ API: Расписание группы S-4, Понедельник, Четная неделя")
print("=" * 60)
print(f"\nURL: {url}\n")

try:
    response = requests.get(url)
    print(f"Статус код: {response.status_code}")
    
    if response.status_code == 200:
        data = response.json()
        print(f"\n✅ Получено занятий: {len(data)}\n")
        
        if data:
            for i, lesson in enumerate(data, 1):
                print(f"{i}. Пара {lesson.get('lessonNumber', 'N/A')}")
                print(f"   Предмет: {lesson.get('subject', 'N/A')[:60]}...")
                print(f"   Преподаватель: {lesson.get('teacher', 'N/A')}")
                print(f"   Аудитория: {lesson.get('classroom', 'N/A')}")
                print(f"   Тип: {lesson.get('lessonType', 'N/A')}, Неделя: {lesson.get('weekParity', 'N/A')}")
                print()
        else:
            print("❌ Занятий не найдено!")
    else:
        print(f"❌ Ошибка: {response.status_code}")
        print(f"Ответ: {response.text}")
        
except Exception as e:
    print(f"❌ Ошибка запроса: {e}")
    import traceback
    traceback.print_exc()
