"""
Скрипт для скачивания файлов с FTP сервера университета
Использование: python download_from_ftp.py
"""
import os
import sys
from ftplib import FTP
from pathlib import Path
from datetime import datetime

# Устанавливаем UTF-8 для вывода на Windows
if sys.platform == 'win32':
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# Параметры подключения к серверу университета
# Данные предоставлены администратором сервера
FTP_HOST = "46.56.85.227"
FTP_PORT = 21
FTP_USER = "rasp"
FTP_PASS = "psar_25"
FTP_FOLDER = "rasp"  # Папка на FTP сервере

# Локальная папка для сохранения
DEFAULT_LOCAL_DIR = os.path.join(os.path.dirname(__file__), "..", "ftp_download")


def log(message: str):
    """Логирование с временной меткой"""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{timestamp}] {message}")


def download_from_ftp(local_dir: str = None):
    """
    Скачивает файлы с FTP сервера
    
    Args:
        local_dir: Локальная папка для сохранения (по умолчанию: ftp_download)
    """
    if local_dir is None:
        local_dir = DEFAULT_LOCAL_DIR
    
    # Создаем локальную папку
    local_path = Path(local_dir)
    local_path.mkdir(parents=True, exist_ok=True)
    
    log("=" * 70)
    log("ПОДКЛЮЧЕНИЕ К FTP СЕРВЕРУ")
    log(f"Сервер: {FTP_HOST}:{FTP_PORT}")
    log(f"Пользователь: {FTP_USER}")
    log(f"Папка: {FTP_FOLDER}")
    log(f"Локальная папка: {local_path}")
    log("=" * 70)
    
    try:
        # Подключаемся к FTP
        log("Подключение к FTP серверу...")
        ftp = FTP()
        ftp.connect(FTP_HOST, FTP_PORT)
        ftp.login(FTP_USER, FTP_PASS)
        log("✓ Подключено успешно!")
        
        # Переходим в нужную папку
        try:
            ftp.cwd(FTP_FOLDER)
            log(f"✓ Перешли в папку: {FTP_FOLDER}")
        except Exception as e:
            log(f"⚠ Папка {FTP_FOLDER} не найдена, используем корневую папку")
        
        # Получаем список файлов
        log("\nПолучение списка файлов...")
        files = []
        try:
            # Пробуем получить детальный список
            ftp.retrlines('LIST', files.append)
            log(f"✓ Найдено записей: {len(files)}")
        except:
            # Если не получилось, пробуем простой список
            files = ftp.nlst()
            log(f"✓ Найдено файлов: {len(files)}")
        
        # Выводим список файлов
        log("\nФайлы на сервере:")
        log("-" * 70)
        file_list = []
        for item in files:
            if isinstance(item, str):
                # Простой список
                if item not in ['.', '..']:
                    file_list.append(item)
                    log(f"  📄 {item}")
            else:
                # Детальный список (парсим строку)
                parts = item.split()
                if len(parts) >= 9:
                    filename = ' '.join(parts[8:])
                    if filename not in ['.', '..']:
                        file_list.append(filename)
                        size = parts[4] if len(parts) > 4 else "?"
                        log(f"  📄 {filename} ({size} байт)")
        
        # Скачиваем файлы
        log("\n" + "=" * 70)
        log("НАЧАЛО СКАЧИВАНИЯ")
        log("=" * 70)
        
        downloaded = 0
        skipped = 0
        errors = 0
        
        for filename in file_list:
            local_file = local_path / filename
            
            # Пропускаем папки
            try:
                # Пробуем определить, это файл или папка
                size = ftp.size(filename)
                if size is None:
                    log(f"⏭ Пропуск {filename} (возможно, папка)")
                    skipped += 1
                    continue
            except:
                pass
            
            # Проверяем, нужно ли скачивать
            if local_file.exists():
                try:
                    local_size = local_file.stat().st_size
                    remote_size = ftp.size(filename)
                    if local_size == remote_size:
                        log(f"⏭ Пропуск {filename} (уже скачан)")
                        skipped += 1
                        continue
                except:
                    pass
            
            # Скачиваем файл
            try:
                log(f"⬇ Скачивание: {filename}...")
                with open(local_file, 'wb') as f:
                    ftp.retrbinary(f'RETR {filename}', f.write)
                
                file_size = local_file.stat().st_size
                log(f"✓ Скачан: {filename} ({file_size:,} байт)")
                downloaded += 1
            except Exception as e:
                log(f"✗ Ошибка скачивания {filename}: {e}")
                errors += 1
                # Удаляем неполный файл
                if local_file.exists():
                    try:
                        local_file.unlink()
                    except:
                        pass
        
        # Закрываем соединение
        ftp.quit()
        
        # Итоги
        log("\n" + "=" * 70)
        log("СКАЧИВАНИЕ ЗАВЕРШЕНО")
        log("=" * 70)
        log(f"✓ Скачано: {downloaded} файлов")
        log(f"⏭ Пропущено: {skipped} файлов")
        if errors > 0:
            log(f"✗ Ошибок: {errors}")
        log(f"📁 Файлы сохранены в: {local_path}")
        log("=" * 70)
        
        return downloaded
        
    except Exception as e:
        log(f"\n✗ КРИТИЧЕСКАЯ ОШИБКА: {e}")
        import traceback
        traceback.print_exc()
        return 0


def main():
    """Главная функция"""
    import argparse
    
    parser = argparse.ArgumentParser(
        description='Скачивание файлов с FTP сервера университета'
    )
    
    parser.add_argument(
        '--dir',
        type=str,
        default=None,
        help='Локальная папка для сохранения (по умолчанию: ftp_download)'
    )
    
    args = parser.parse_args()
    
    print("=" * 70)
    print("СКАЧИВАНИЕ ФАЙЛОВ С FTP СЕРВЕРА")
    print("=" * 70)
    print()
    
    downloaded = download_from_ftp(args.dir)
    
    print()
    if downloaded > 0:
        print("✓ Файлы успешно скачаны!")
        print("\nСледующий шаг:")
        print("1. Проверьте скачанные файлы")
        print("2. Если это Excel файлы - обработайте через batch_parser.py")
        print("3. Если это JSON/XML - используйте для API")
    else:
        print("⚠ Файлы не скачаны. Проверьте подключение и права доступа.")
    print()


if __name__ == '__main__':
    main()

