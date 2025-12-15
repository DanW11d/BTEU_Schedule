"""Тест API для проверки доступности расписания"""
import requests
import sys
import io

# Исправление кодировки для Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def test_schedule(group_code="П-2", day=1, week="odd"):
    """Проверяет доступность расписания через API"""
    try:
        url = f"http://localhost:8000/v1/schedule/group/{group_code}/day/{day}"
        if week:
            url += f"?week={week}"
        
        print(f"Проверка API: {url}")
        response = requests.get(url)
        
        if response.status_code == 200:
            lessons = response.json()
            print(f"✓ API работает!")
            print(f"✓ Найдено занятий: {len(lessons)}")
            if lessons:
                print("\nПервые занятия:")
                for lesson in lessons[:3]:
                    print(f"  - Пара {lesson.get('lessonNumber')}: {lesson.get('subject')}")
                    print(f"    Преподаватель: {lesson.get('teacher', 'не указан')}")
                    print(f"    Аудитория: {lesson.get('classroom', 'не указана')}")
            return True
        elif response.status_code == 404:
            print(f"[!] Группа '{group_code}' не найдена в БД")
            return False
        else:
            print(f"[X] Ошибка API: {response.status_code}")
            print(f"  Ответ: {response.text}")
            return False
    except requests.exceptions.ConnectionError:
        print("[X] Сервер не запущен! Запустите: python backend/server.py")
        return False
    except Exception as e:
        print(f"[X] Ошибка: {e}")
        return False

if __name__ == "__main__":
    # Тестируем несколько групп
    groups_to_test = ["П-2", "Л-3", "Б-4", "А-1"]
    
    print("=" * 60)
    print("ПРОВЕРКА ДОСТУПНОСТИ РАСПИСАНИЯ В БД")
    print("=" * 60)
    
    success_count = 0
    for group in groups_to_test:
        print(f"\n[Группа: {group}]")
        if test_schedule(group, day=1, week="odd"):
            success_count += 1
    
    print("\n" + "=" * 60)
    print(f"ИТОГО: {success_count}/{len(groups_to_test)} групп имеют расписание")
    print("=" * 60)
    
    if success_count > 0:
        print("\n[OK] Расписание доступно! Приложение должно показывать данные.")
    else:
        print("\n[!] Расписание не найдено. Проверьте:")
        print("  1. Запущен ли сервер (python backend/server.py)")
        print("  2. Сохранены ли данные в БД (запустите batch_parser.py)")

