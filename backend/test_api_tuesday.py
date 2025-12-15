"""Тест API для вторника"""
import requests
import json
import sys
import io

# Исправление кодировки для Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

url_odd = "http://localhost:8000/v1/schedule/group/S-4/day/2?week=odd"
url_even = "http://localhost:8000/v1/schedule/group/S-4/day/2?week=even"

print("=" * 80)
print("ТЕСТ API: ВТОРНИК")
print("=" * 80)

for week_name, url in [("НЕЧЕТНАЯ", url_odd), ("ЧЕТНАЯ", url_even)]:
    print(f"\n{week_name} НЕДЕЛЯ:")
    print(f"URL: {url}\n")
    
    try:
        response = requests.get(url, timeout=5)
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Получено занятий: {len(data)}\n")
            
            for lesson in data:
                print(f"  Пара {lesson.get('lessonNumber')}: {lesson.get('subject')[:60]}")
                print(f"    lessonType: {lesson.get('lessonType')}")
                print(f"    ID: {lesson.get('id')}")
                print()
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
