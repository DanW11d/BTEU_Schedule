"""Тест API для проверки lessonType"""
import requests
import json
import sys
import io

# Исправление кодировки для Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

url = "http://localhost:8000/v1/schedule/group/S-4/day/1?week=odd"

print("=" * 80)
print("ТЕСТ API: ПОНЕДЕЛЬНИК, НЕЧЕТНАЯ НЕДЕЛЯ")
print("=" * 80)
print(f"URL: {url}\n")

try:
    response = requests.get(url, timeout=5)
    
    if response.status_code == 200:
        data = response.json()
        print(f"✅ Получено занятий: {len(data)}\n")
        
        for lesson in data:
            if 'интернет' in lesson.get('subject', '').lower() or 'маркетинг' in lesson.get('subject', '').lower():
                print("=" * 80)
                print("НАЙДЕНО: Интернет-маркетинг")
                print("=" * 80)
                print(f"ID: {lesson.get('id')}")
                print(f"Предмет: {lesson.get('subject')}")
                print(f"lessonType: {lesson.get('lessonType')}")
                print(f"lessonNumber: {lesson.get('lessonNumber')}")
                print(f"dayOfWeek: {lesson.get('dayOfWeek')}")
                print(f"weekParity: {lesson.get('weekParity')}")
                print()
        
        # Показываем все занятия для пары 1
        print("=" * 80)
        print("ВСЕ ЗАНЯТИЯ ДЛЯ ПАРЫ 1:")
        print("=" * 80)
        pair1_lessons = [l for l in data if l.get('lessonNumber') == 1]
        for lesson in pair1_lessons:
            print(f"\nПредмет: {lesson.get('subject')}")
            print(f"  lessonType: {lesson.get('lessonType')}")
            print(f"  ID: {lesson.get('id')}")
    else:
        print(f"❌ Ошибка: {response.status_code}")
        print(response.text)
        
except requests.exceptions.ConnectionError:
    print("❌ Не удалось подключиться к серверу. Убедитесь, что сервер запущен:")
    print("   python backend/server.py")
except Exception as e:
    print(f"❌ Ошибка: {e}")
    import traceback
    traceback.print_exc()

