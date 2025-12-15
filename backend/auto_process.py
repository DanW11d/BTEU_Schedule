"""
Автоматизация процесса: скачать → распарсить → загрузить в БД
Интегрирует скрипт загрузки файлов с парсером
"""
import os
import sys
import argparse
import subprocess
import json
from pathlib import Path
from typing import Optional
import requests
from dotenv import load_dotenv

# Загружаем переменные окружения
load_dotenv()


def check_api_health(api_url: str = "http://localhost:8000") -> bool:
    """
    Проверяет доступность API сервера
    
    Args:
        api_url: URL API сервера
        
    Returns:
        True если сервер доступен, False иначе
    """
    try:
        response = requests.get(f"{api_url}/v1/health", timeout=5)
        return response.status_code == 200
    except:
        return False


def run_download_script(download_script_path: Optional[str] = None) -> bool:
    """
    Запускает скрипт загрузки файлов с сайта
    
    Args:
        download_script_path: Путь к скрипту загрузки (если None, используется стандартный)
        
    Returns:
        True если скрипт выполнен успешно, False иначе
    """
    if download_script_path and os.path.exists(download_script_path):
        print(f"Запуск скрипта загрузки: {download_script_path}")
        try:
            # Если это Jupyter notebook, нужно конвертировать или запустить через jupyter
            if download_script_path.endswith('.ipynb'):
                print("⚠ Обнаружен Jupyter notebook. Рекомендуется запустить его вручную.")
                print("   Или конвертировать в .py скрипт для автоматического запуска.")
                return False
            
            # Запускаем Python скрипт
            result = subprocess.run(
                [sys.executable, download_script_path],
                capture_output=True,
                text=True,
                encoding='utf-8'
            )
            
            if result.returncode == 0:
                print("✓ Скрипт загрузки выполнен успешно")
                return True
            else:
                print(f"❌ Ошибка выполнения скрипта загрузки:")
                print(result.stderr)
                return False
        except Exception as e:
            print(f"❌ Ошибка запуска скрипта: {e}")
            return False
    else:
        print("⚠ Скрипт загрузки не указан или не найден")
        print("   Пропускаем этап загрузки файлов")
        return True  # Не критично, продолжаем


def process_directory_via_api(
    directory: str,
    api_url: str = "http://localhost:8000"
) -> dict:
    """
    Обрабатывает директорию через API endpoint batch-parse
    
    Args:
        directory: Путь к директории с Excel файлами
        api_url: URL API сервера
        
    Returns:
        Словарь с результатами обработки
    """
    print(f"\n📤 Отправка запроса на пакетную обработку...")
    print(f"   Директория: {directory}")
    print(f"   API: {api_url}")
    
    try:
        response = requests.post(
            f"{api_url}/v1/admin/batch-parse",
            json={'directory': directory},
            timeout=300  # 5 минут на обработку
        )
        
        if response.status_code == 200:
            result = response.json()
            return {
                'success': True,
                'data': result
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


def auto_process(
    xls_directory: str,
    api_url: str = "http://localhost:8000",
    download_script: Optional[str] = None,
    skip_download: bool = False
) -> dict:
    """
    Автоматизирует весь процесс: скачать → распарсить → загрузить в БД
    
    Args:
        xls_directory: Путь к директории с Excel файлами (XLS_DIR)
        api_url: URL API сервера
        download_script: Путь к скрипту загрузки файлов (опционально)
        skip_download: Пропустить этап загрузки файлов
        
    Returns:
        Словарь с результатами всего процесса
    """
    print("=" * 70)
    print("АВТОМАТИЧЕСКАЯ ОБРАБОТКА РАСПИСАНИЯ")
    print("=" * 70)
    print()
    
    results = {
        'download': {'success': False, 'skipped': skip_download},
        'api_check': {'success': False},
        'processing': {'success': False}
    }
    
    # Шаг 1: Проверка API
    print("Шаг 1: Проверка доступности API сервера...")
    if not check_api_health(api_url):
        print(f"❌ API сервер недоступен: {api_url}")
        print("   Убедитесь, что сервер запущен: python backend/server.py")
        return results
    print(f"✓ API сервер доступен: {api_url}")
    results['api_check']['success'] = True
    print()
    
    # Шаг 2: Загрузка файлов (если нужно)
    if not skip_download and download_script:
        print("Шаг 2: Загрузка файлов с сайта...")
        download_success = run_download_script(download_script)
        results['download']['success'] = download_success
        if not download_success:
            print("⚠ Продолжаем без загрузки новых файлов")
        print()
    elif skip_download:
        print("Шаг 2: Пропущен (--skip-download)")
        results['download']['skipped'] = True
        print()
    else:
        print("Шаг 2: Пропущен (скрипт загрузки не указан)")
        results['download']['skipped'] = True
        print()
    
    # Шаг 3: Обработка файлов через API
    print("Шаг 3: Обработка Excel файлов...")
    if not os.path.isdir(xls_directory):
        print(f"❌ Директория не существует: {xls_directory}")
        results['processing']['error'] = f'Директория не существует: {xls_directory}'
        return results
    
    process_result = process_directory_via_api(xls_directory, api_url)
    
    if process_result['success']:
        data = process_result['data']
        print("✓ Обработка завершена успешно!")
        print()
        print("Результаты:")
        print(f"  Всего файлов:     {data.get('total_files', 0)}")
        print(f"  Обработано:       {data.get('processed', 0)}")
        print(f"  Успешно:          {data.get('success', 0)}")
        print(f"  Ошибок:           {data.get('failed', 0)}")
        print(f"  Пропущено:        {data.get('skipped', 0)}")
        
        results['processing']['success'] = True
        results['processing']['data'] = data
    else:
        print(f"❌ Ошибка обработки: {process_result.get('error', 'Неизвестная ошибка')}")
        results['processing']['error'] = process_result.get('error')
    
    print()
    print("=" * 70)
    
    return results


def main():
    """Главная функция для запуска из командной строки"""
    parser = argparse.ArgumentParser(
        description='Автоматизация процесса обработки расписания: скачать → распарсить → загрузить в БД',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Примеры использования:
  # Полный автоматический процесс
  python auto_process.py --xls-dir "G:\\...\\3 XLS" --download-script "path/to/load_files.py"
  
  # Только обработка (без загрузки)
  python auto_process.py --xls-dir "G:\\...\\3 XLS" --skip-download
  
  # С указанием API URL
  python auto_process.py --xls-dir "G:\\...\\3 XLS" --api-url http://localhost:8000
        """
    )
    
    parser.add_argument(
        '--xls-dir',
        type=str,
        required=True,
        help='Путь к директории с Excel файлами (XLS_DIR)'
    )
    
    parser.add_argument(
        '--api-url',
        type=str,
        default='http://localhost:8000',
        help='URL API сервера (по умолчанию: http://localhost:8000)'
    )
    
    parser.add_argument(
        '--download-script',
        type=str,
        help='Путь к скрипту загрузки файлов с сайта'
    )
    
    parser.add_argument(
        '--skip-download',
        action='store_true',
        help='Пропустить этап загрузки файлов'
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
    
    # Запускаем автоматическую обработку
    results = auto_process(
        xls_directory=args.xls_dir,
        api_url=args.api_url,
        download_script=args.download_script,
        skip_download=args.skip_download
    )
    
    # Сохраняем результаты в JSON если указан файл
    if args.output:
        with open(args.output, 'w', encoding='utf-8') as f:
            json.dump(results, f, ensure_ascii=False, indent=2)
        print(f"\nРезультаты сохранены в: {args.output}")
    
    # Возвращаем код выхода
    if results['processing'].get('success'):
        sys.exit(0)
    else:
        sys.exit(1)


if __name__ == '__main__':
    main()

