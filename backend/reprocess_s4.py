"""Переобработка файлов S-4 с исправленным парсером"""
import os
import sys
import io
import requests
from file_processor import extract_group_code, parse_filename

# Исправление кодировки для Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

API_URL = "http://localhost:8000/v1/admin/parse-excel"
EXCEL_DIR = r"D:\Excel file"

def reprocess_s4():
    """Переобрабатывает файлы S-4"""
    print("=" * 60)
    print("ПЕРЕОБРАБОТКА ФАЙЛОВ S-4")
    print("=" * 60)
    
    # Сначала удаляем старые занятия группы S-4
    print("\n1. Удаление старых занятий группы S-4...")
    try:
        delete_url = "http://localhost:8000/v1/admin/groups/51/lessons"
        response = requests.delete(delete_url)
        if response.status_code == 200:
            print("   ✅ Старые занятия удалены")
        else:
            print(f"   ⚠ Не удалось удалить старые занятия: {response.status_code}")
    except Exception as e:
        print(f"   ⚠ Ошибка при удалении: {e}")
    
    # Находим файлы S-4
    if not os.path.exists(EXCEL_DIR):
        print(f"\n❌ Директория {EXCEL_DIR} не существует!")
        return
    
    files = [f for f in os.listdir(EXCEL_DIR) 
             if f.lower().startswith('s-4') and (f.endswith('.xls') or f.endswith('.xlsx'))]
    
    if not files:
        print(f"\n❌ Файлы S-4 не найдены в {EXCEL_DIR}")
        return
    
    print(f"\n2. Найдено файлов: {len(files)}\n")
    
    for filename in files:
        filepath = os.path.join(EXCEL_DIR, filename)
        print(f"[{files.index(filename) + 1}/{len(files)}] Обработка: {filename}")
        
        # Принудительно используем S-4
        group_code = 'S-4'
        _, modification_date = parse_filename(filename)
        
        print(f"  Код группы: {group_code}")
        if modification_date:
            print(f"  Дата модификации: {modification_date}")
        
        # Отправляем на парсинг
        try:
            with open(filepath, 'rb') as f:
                files_data = {'file': (filename, f, 'application/vnd.ms-excel' if filename.endswith('.xls') else 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')}
                response = requests.post(API_URL, files=files_data, data={'group_code': group_code})
            
            if response.status_code == 200:
                result = response.json()
                if result.get('success'):
                    parsed = result.get('parsed_count', 0)
                    saved = result.get('saved_count', 0)
                    print(f"  ✅ Успешно обработано!")
                    print(f"    Занятий распарсено: {parsed}")
                    print(f"    Занятий сохранено: {saved}")
                else:
                    print(f"  [!] Обработано, но занятий не найдено")
            else:
                error = response.json().get('error', 'Неизвестная ошибка')
                print(f"  [X] Ошибка: {error}")
        except Exception as e:
            print(f"  [X] Ошибка при обработке: {e}")
        
        print()
    
    print("=" * 60)
    print("ПЕРЕОБРАБОТКА ЗАВЕРШЕНА")
    print("=" * 60)
    print("\nПроверьте расписание в приложении!")

if __name__ == "__main__":
    reprocess_s4()

