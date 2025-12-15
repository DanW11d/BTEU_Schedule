"""
Пакетная обработка Excel файлов с расписанием
Сканирует папку, извлекает коды групп, парсит и загружает в БД через API
"""
import os
import sys
import argparse
import requests
from pathlib import Path
from typing import List, Dict, Optional, Tuple
import json
from datetime import datetime
from file_processor import extract_group_code, parse_filename
import psycopg2
from psycopg2.extras import RealDictCursor
from dotenv import load_dotenv

# Загружаем переменные окружения
load_dotenv()

# Настройки подключения к БД (для получения group_id)
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': int(os.getenv('DB_PORT', '5432')),
    'database': os.getenv('DB_NAME', 'postgres'),
    'user': os.getenv('DB_USER', 'postgres'),
    'password': os.getenv('DB_PASSWORD', '7631')
}


def get_group_id_from_db(group_code: str) -> Optional[int]:
    """
    Получает group_id из БД по коду группы
    
    Args:
        group_code: Код группы (например, "П-1", "Э-2")
        
    Returns:
        ID группы или None если не найдена
    """
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute(
            "SELECT id FROM groups WHERE code = %s AND is_active = TRUE",
            (group_code,)
        )
        result = cur.fetchone()
        cur.close()
        conn.close()
        
        return result['id'] if result else None
    except Exception as e:
        print(f"Ошибка получения group_id для {group_code}: {e}")
        return None


def parse_file_via_api(
    file_path: str,
    group_code: str,
    group_id: Optional[int],
    api_url: str = "http://localhost:8000"
) -> Dict:
    """
    Парсит файл через API endpoint
    
    Args:
        file_path: Путь к Excel файлу
        group_code: Код группы
        group_id: ID группы (опционально, если None - будет найден через API)
        api_url: URL API сервера
        
    Returns:
        Словарь с результатом обработки
    """
    try:
        url = f"{api_url}/v1/admin/parse-excel"
        
        with open(file_path, 'rb') as f:
            files = {'file': (os.path.basename(file_path), f, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')}
            data = {'group_code': group_code}
            
            if group_id:
                data['group_id'] = str(group_id)
            
            response = requests.post(url, files=files, data=data, timeout=60)
            
            if response.status_code == 200:
                result = response.json()
                return {
                    'success': True,
                    'message': result.get('message', 'Успешно обработано'),
                    'lessons_parsed': result.get('lessons_parsed', 0),
                    'lessons_saved': result.get('lessons_saved', 0),
                    'errors': result.get('errors')
                }
            else:
                error_data = response.json() if response.headers.get('content-type', '').startswith('application/json') else {}
                return {
                    'success': False,
                    'error': error_data.get('error', f'HTTP {response.status_code}'),
                    'status_code': response.status_code
                }
    except requests.exceptions.RequestException as e:
        return {
            'success': False,
            'error': f'Ошибка запроса к API: {str(e)}'
        }
    except Exception as e:
        return {
            'success': False,
            'error': f'Неожиданная ошибка: {str(e)}'
        }


def scan_directory(directory: str, extensions: Tuple[str, ...] = ('.xlsx', '.xls')) -> List[Path]:
    """
    Сканирует директорию и возвращает список Excel файлов
    
    Args:
        directory: Путь к директории
        extensions: Кортеж расширений файлов
        
    Returns:
        Список путей к файлам
    """
    directory_path = Path(directory)
    if not directory_path.exists():
        return []
    
    files = []
    for ext in extensions:
        files.extend(directory_path.glob(f'*{ext}'))
    
    return sorted(files)


def batch_parse_directory(
    directory: str,
    api_url: str = "http://localhost:8000",
    dry_run: bool = False,
    skip_errors: bool = True,
    verbose: bool = True
) -> Dict:
    """
    Обрабатывает все Excel файлы в директории
    
    Args:
        directory: Путь к папке с Excel файлами
        api_url: URL API сервера
        dry_run: Если True, только показывает что будет сделано (не выполняет)
        skip_errors: Пропускать файлы с ошибками и продолжать обработку
        verbose: Выводить подробную информацию
        
    Returns:
        Словарь с результатами обработки
    """
    print("=" * 70)
    print("ПАКЕТНАЯ ОБРАБОТКА EXCEL ФАЙЛОВ")
    print("=" * 70)
    print(f"Директория: {directory}")
    print(f"API URL: {api_url}")
    print(f"Режим: {'DRY RUN (тестовый)' if dry_run else 'РЕАЛЬНАЯ ОБРАБОТКА'}")
    print("=" * 70)
    print()
    
    # Сканируем директорию
    files = scan_directory(directory)
    
    if not files:
        print(f"❌ Не найдено Excel файлов в директории: {directory}")
        return {
            'total_files': 0,
            'processed': 0,
            'success': 0,
            'failed': 0,
            'skipped': 0,
            'results': []
        }
    
    print(f"Найдено файлов: {len(files)}\n")
    
    results = {
        'total_files': len(files),
        'processed': 0,
        'success': 0,
        'failed': 0,
        'skipped': 0,
        'results': []
    }
    
    # Обрабатываем каждый файл
    for idx, file_path in enumerate(files, 1):
        filename = file_path.name
        print(f"[{idx}/{len(files)}] Обработка: {filename}")
        
        # Извлекаем код группы из имени файла
        group_code, modification_date = parse_filename(filename)
        
        if not group_code:
            error_msg = f"Не удалось извлечь код группы из имени файла"
            print(f"  ❌ {error_msg}\n")
            results['failed'] += 1
            results['results'].append({
                'file': filename,
                'success': False,
                'error': error_msg
            })
            if not skip_errors:
                break
            continue
        
        print(f"  Код группы: {group_code}")
        if modification_date:
            print(f"  Дата модификации: {modification_date}")
        
        # Получаем group_id из БД
        group_id = get_group_id_from_db(group_code)
        
        if not group_id:
            error_msg = f"Группа '{group_code}' не найдена в БД"
            print(f"  ❌ {error_msg}\n")
            results['skipped'] += 1
            results['results'].append({
                'file': filename,
                'group_code': group_code,
                'success': False,
                'error': error_msg
            })
            if not skip_errors:
                break
            continue
        
        print(f"  ID группы: {group_id}")
        
        if dry_run:
            print(f"  ✓ [DRY RUN] Будет обработан: {filename} → {group_code} (ID: {group_id})\n")
            results['processed'] += 1
            results['results'].append({
                'file': filename,
                'group_code': group_code,
                'group_id': group_id,
                'success': True,
                'dry_run': True
            })
            continue
        
        # Парсим файл через API
        print(f"  Отправка на парсинг...")
        result = parse_file_via_api(str(file_path), group_code, group_id, api_url)
        
        if result['success']:
            lessons_parsed = result.get('lessons_parsed', 0)
            lessons_saved = result.get('lessons_saved', 0)
            warnings = result.get('warnings', [])
            
            if lessons_saved > 0:
                print(f"  ✓ Успешно обработано!")
                print(f"    Занятий распарсено: {lessons_parsed}")
                print(f"    Занятий сохранено: {lessons_saved}")
            else:
                print(f"  ⚠ Обработано, но занятий не найдено")
                if warnings:
                    print(f"    Причина: {warnings[0]}")
            
            if result.get('errors'):
                print(f"    ⚠ Предупреждения: {len(result['errors'])}")
            print()
            
            results['success'] += 1
            results['results'].append({
                'file': filename,
                'group_code': group_code,
                'group_id': group_id,
                'success': True,
                'lessons_parsed': lessons_parsed,
                'lessons_saved': lessons_saved,
                'errors': result.get('errors')
            })
        else:
            error_msg = result.get('error', 'Неизвестная ошибка')
            errors = result.get('errors', [])
            errors_count = result.get('errors_count', 0)
            lessons_count = result.get('lessons_count', 0)
            
            print(f"  ❌ Ошибка: {error_msg}")
            if errors_count and lessons_count:
                print(f"    Ошибок валидации: {errors_count} из {lessons_count} занятий")
                if errors and len(errors) > 0:
                    print(f"    Примеры ошибок:")
                    for err in errors[:3]:
                        print(f"      - {err}")
            print()
            results['failed'] += 1
            results['results'].append({
                'file': filename,
                'group_code': group_code,
                'group_id': group_id,
                'success': False,
                'error': error_msg
            })
            if not skip_errors:
                break
            continue
        
        results['processed'] += 1
    
    # Итоговая статистика
    print("=" * 70)
    print("ИТОГОВАЯ СТАТИСТИКА")
    print("=" * 70)
    print(f"Всего файлов:        {results['total_files']}")
    print(f"Обработано:          {results['processed']}")
    print(f"Успешно:             {results['success']}")
    print(f"Ошибок:              {results['failed']}")
    print(f"Пропущено:           {results['skipped']}")
    print("=" * 70)
    
    return results


def main():
    """Главная функция для запуска из командной строки"""
    parser = argparse.ArgumentParser(
        description='Пакетная обработка Excel файлов с расписанием',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Примеры использования:
  # Тестовый запуск (dry run)
  python batch_parser.py --dir "G:\\...\\3 XLS" --dry-run
  
  # Реальная обработка
  python batch_parser.py --dir "G:\\...\\3 XLS" --api-url http://localhost:8000
  
  # Обработка с остановкой при ошибке
  python batch_parser.py --dir "G:\\...\\3 XLS" --no-skip-errors
        """
    )
    
    parser.add_argument(
        '--dir',
        type=str,
        required=True,
        help='Путь к директории с Excel файлами'
    )
    
    parser.add_argument(
        '--api-url',
        type=str,
        default='http://localhost:8000',
        help='URL API сервера (по умолчанию: http://localhost:8000)'
    )
    
    parser.add_argument(
        '--dry-run',
        action='store_true',
        help='Тестовый режим (не выполняет реальную обработку)'
    )
    
    parser.add_argument(
        '--no-skip-errors',
        action='store_true',
        help='Останавливать обработку при первой ошибке'
    )
    
    parser.add_argument(
        '--output',
        type=str,
        help='Путь к JSON файлу для сохранения результатов'
    )
    
    args = parser.parse_args()
    
    # Устанавливаем UTF-8 для вывода на Windows
    if sys.platform == 'win32':
        import io
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    
    # Запускаем обработку
    results = batch_parse_directory(
        directory=args.dir,
        api_url=args.api_url,
        dry_run=args.dry_run,
        skip_errors=not args.no_skip_errors,
        verbose=True
    )
    
    # Сохраняем результаты в JSON если указан файл
    if args.output:
        with open(args.output, 'w', encoding='utf-8') as f:
            json.dump(results, f, ensure_ascii=False, indent=2)
        print(f"\nРезультаты сохранены в: {args.output}")
    
    # Возвращаем код выхода
    sys.exit(0 if results['failed'] == 0 else 1)


if __name__ == '__main__':
    main()

