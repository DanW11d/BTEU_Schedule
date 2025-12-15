"""
Скрипт для автоматической синхронизации данных с FTP сервера
Скачивает новые файлы, обрабатывает их и обновляет базу данных

Использование:
    python sync_from_ftp.py
    python sync_from_ftp.py --auto-process    # Автоматически обработать файлы
"""
import os
import sys
from pathlib import Path
from datetime import datetime
from ftplib import FTP

# Добавляем путь к модулям
sys.path.insert(0, os.path.dirname(__file__))

from download_from_ftp import download_from_ftp, FTP_HOST, FTP_USER, FTP_PASS, FTP_FOLDER

# Импорты для обработки файлов
try:
    from batch_parser import main as batch_parse_main
    BATCH_PARSER_AVAILABLE = True
except ImportError:
    BATCH_PARSER_AVAILABLE = False
    print("⚠ batch_parser.py не найден, автоматическая обработка недоступна")


def log(message: str):
    """Логирование с временной меткой"""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{timestamp}] {message}")


def check_ftp_connection():
    """Проверяет подключение к FTP серверу"""
    log("Проверка подключения к FTP серверу...")
    try:
        ftp = FTP()
        ftp.connect(FTP_HOST, 21)
        ftp.login(FTP_USER, FTP_PASS)
        try:
            ftp.cwd(FTP_FOLDER)
            log(f"✓ Подключение успешно! Папка: {FTP_FOLDER}")
        except:
            log(f"✓ Подключение успешно! (папка {FTP_FOLDER} не найдена)")
        ftp.quit()
        return True
    except Exception as e:
        log(f"✗ Ошибка подключения к FTP: {e}")
        return False


def sync_from_ftp(auto_process: bool = False, download_dir: str = None):
    """
    Синхронизирует данные с FTP сервера
    
    Args:
        auto_process: Автоматически обработать скачанные файлы
        download_dir: Папка для скачивания файлов
    """
    log("=" * 70)
    log("СИНХРОНИЗАЦИЯ С FTP СЕРВЕРОМ")
    log("=" * 70)
    log(f"Сервер: {FTP_HOST}")
    log(f"Пользователь: {FTP_USER}")
    log(f"Папка: {FTP_FOLDER}")
    log("=" * 70)
    
    # Проверка подключения
    if not check_ftp_connection():
        log("\n✗ Не удалось подключиться к FTP серверу")
        log("Проверьте:")
        log("  - Подключение к интернету")
        log("  - Данные подключения (host, user, password)")
        log("  - Доступность FTP сервера")
        return False
    
    # Скачивание файлов
    log("\n" + "=" * 70)
    log("ШАГ 1: СКАЧИВАНИЕ ФАЙЛОВ С FTP")
    log("=" * 70)
    
    downloaded = download_from_ftp(download_dir)
    
    if downloaded == 0:
        log("\n⚠ Новых файлов не найдено")
        log("Файлы могут быть уже скачаны или сервер пуст")
    else:
        log(f"\n✓ Скачано файлов: {downloaded}")
    
    # Автоматическая обработка
    if auto_process and BATCH_PARSER_AVAILABLE and downloaded > 0:
        log("\n" + "=" * 70)
        log("ШАГ 2: ОБРАБОТКА ФАЙЛОВ")
        log("=" * 70)
        
        if download_dir is None:
            download_dir = os.path.join(os.path.dirname(__file__), "..", "ftp_download")
        
        log(f"Обработка файлов из: {download_dir}")
        
        try:
            # Сохраняем аргументы командной строки
            old_argv = sys.argv
            
            # Устанавливаем аргументы для batch_parser
            sys.argv = ['batch_parser.py', '--dir', download_dir]
            
            # Запускаем обработку
            batch_parse_main()
            
            # Восстанавливаем аргументы
            sys.argv = old_argv
            
            log("\n✓ Обработка завершена!")
        except Exception as e:
            log(f"\n✗ Ошибка обработки файлов: {e}")
            import traceback
            traceback.print_exc()
            sys.argv = old_argv
    elif auto_process and not BATCH_PARSER_AVAILABLE:
        log("\n⚠ Автоматическая обработка недоступна")
        log("batch_parser.py не найден")
    elif auto_process and downloaded == 0:
        log("\n⏭ Автоматическая обработка пропущена (нет новых файлов)")
    
    # Итоги
    log("\n" + "=" * 70)
    log("СИНХРОНИЗАЦИЯ ЗАВЕРШЕНА")
    log("=" * 70)
    
    if downloaded > 0:
        log(f"✓ Скачано файлов: {downloaded}")
        if not auto_process:
            log("\nСледующий шаг: обработайте файлы вручную:")
            if download_dir:
                log(f"  python backend/batch_parser.py --dir \"{download_dir}\"")
            else:
                log("  python backend/batch_parser.py --dir ftp_download")
    else:
        log("✓ Все файлы актуальны")
    
    log("=" * 70)
    
    return True


def main():
    """Главная функция"""
    import argparse
    
    parser = argparse.ArgumentParser(
        description='Синхронизация данных с FTP сервера университета'
    )
    
    parser.add_argument(
        '--auto-process',
        action='store_true',
        help='Автоматически обработать скачанные файлы'
    )
    
    parser.add_argument(
        '--dir',
        type=str,
        default=None,
        help='Папка для скачивания файлов (по умолчанию: ftp_download)'
    )
    
    args = parser.parse_args()
    
    print("=" * 70)
    print("СИНХРОНИЗАЦИЯ С FTP СЕРВЕРОМ")
    print("=" * 70)
    print()
    
    success = sync_from_ftp(
        auto_process=args.auto_process,
        download_dir=args.dir
    )
    
    if success:
        print("\n✓ Синхронизация завершена!")
    else:
        print("\n✗ Синхронизация завершена с ошибками")
        sys.exit(1)


if __name__ == '__main__':
    main()

