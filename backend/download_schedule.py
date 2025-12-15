"""
Скрипт для скачивания Excel файлов с расписанием с сайта BTEU
Скачивает файлы в указанную директорию для последующей обработки
"""
import os
import sys
import requests
from bs4 import BeautifulSoup
from pathlib import Path
from datetime import datetime
from urllib.parse import urljoin
from email.utils import parsedate_to_datetime
from requests.adapters import HTTPAdapter, Retry
import json
import io

# Устанавливаем UTF-8 для вывода на Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')


class ScheduleDownloader:
    """Класс для скачивания расписания с сайта BTEU"""
    
    BASE_URL = "https://bteu.by/studentu/obrazovanie-i-praktika/raspisanie-zanyatij"
    
    def __init__(self, download_dir: str):
        """
        Инициализация загрузчика
        
        Args:
            download_dir: Директория для сохранения файлов
        """
        self.download_dir = Path(download_dir)
        self.download_dir.mkdir(parents=True, exist_ok=True)
        
        # Создаем сессию с повторными попытками
        self.session = requests.Session()
        retries = Retry(total=3, backoff_factor=0.5, status_forcelist=[429, 500, 502, 503, 504])
        self.session.mount("http://", HTTPAdapter(max_retries=retries))
        self.session.mount("https://", HTTPAdapter(max_retries=retries))
        self.session.headers.update({
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/91.0.4472.124"
        })
        
        # Манифест для отслеживания скачанных файлов
        self.manifest_path = self.download_dir / ".manifest.json"
        self.manifest = self._load_manifest()
        self.new_files = []
    
    def _load_manifest(self) -> dict:
        """Загружает манифест скачанных файлов"""
        if self.manifest_path.exists():
            try:
                return json.loads(self.manifest_path.read_text(encoding='utf-8'))
            except:
                return {}
        return {}
    
    def _save_manifest(self):
        """Сохраняет манифест"""
        self.manifest_path.write_text(
            json.dumps(self.manifest, ensure_ascii=False, indent=2),
            encoding='utf-8'
        )
    
    def _log(self, message: str):
        """Логирование"""
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        log_message = f"[{timestamp}] {message}"
        print(log_message)
    
    def _get_links(self, url: str) -> list:
        """Получает все ссылки со страницы"""
        try:
            r = self.session.get(url, timeout=10)
            r.raise_for_status()
            soup = BeautifulSoup(r.text, "html.parser")
            return [urljoin(url, a["href"]) for a in soup.find_all("a", href=True)]
        except Exception as e:
            self._log(f"Ошибка доступа к {url}: {e}")
            return []
    
    def _is_excel_file(self, url: str) -> bool:
        """Проверяет, является ли ссылка Excel файлом"""
        return url.lower().endswith(('.xls', '.xlsx'))
    
    def _get_file_modification_date(self, url: str) -> str:
        """Получает дату модификации файла"""
        try:
            response = self.session.head(url, timeout=10)
            response.raise_for_status()
            last_modified = response.headers.get("Last-Modified")
            if last_modified:
                date_obj = parsedate_to_datetime(last_modified)
                return date_obj.strftime("%d.%m.%y")
        except:
            pass
        return "unknown"
    
    def _should_download(self, url: str, headers: dict) -> bool:
        """Определяет, нужно ли скачивать файл"""
        server_len = int(headers.get("Content-Length", 0)) if headers.get("Content-Length", "").isdigit() else None
        server_lm = headers.get("Last-Modified")
        server_etag = headers.get("ETag")
        
        old = self.manifest.get(url, {})
        if server_etag and old.get("etag") == server_etag:
            return False
        if server_len == old.get("size") and server_lm == old.get("last_modified"):
            return False
        return True
    
    def _download_file(self, url: str):
        """Скачивает файл"""
        orig_name = os.path.basename(url)
        name_part, ext = os.path.splitext(orig_name)
        mod_date = self._get_file_modification_date(url)
        new_name = f"{name_part} ({mod_date}){ext}"
        file_path = self.download_dir / new_name
        
        # Проверяем, нужно ли скачивать
        need = True
        try:
            h = self.session.head(url, timeout=10, allow_redirects=True)
            h.raise_for_status()
            need = self._should_download(url, h.headers)
        except:
            pass
        
        if not need and file_path.exists():
            self._log(f"Пропуск {new_name} (не изменился)")
            return
        
        try:
            with self.session.get(url, stream=True, timeout=20) as r:
                r.raise_for_status()
                with open(file_path, "wb") as f:
                    for chunk in r.iter_content(8192):
                        if chunk:
                            f.write(chunk)
            
            self.manifest[url] = {
                "size": os.path.getsize(file_path),
                "last_modified": r.headers.get("Last-Modified"),
                "etag": r.headers.get("ETag"),
                "local_name": new_name
            }
            self.new_files.append(str(file_path))
            self._log(f"✓ Скачан: {new_name}")
        except Exception as e:
            self._log(f"✗ Ошибка скачивания {orig_name}: {e}")
    
    def download_all(self):
        """Скачивает все Excel файлы с сайта"""
        self._log("=" * 70)
        self._log(f"НАЧАЛО СКАНИРОВАНИЯ: {self.BASE_URL}")
        self._log(f"Директория сохранения: {self.download_dir}")
        self._log("=" * 70)
        
        main_links = self._get_links(self.BASE_URL)
        subfolders = [
            l for l in main_links 
            if self.BASE_URL in l and not self._is_excel_file(l) and l != self.BASE_URL and l.endswith("/")
        ]
        
        for sub in subfolders:
            sub_name = Path(sub.rstrip("/")).name
            self._log(f"\n>>> Подпапка: {sub_name}")
            
            links = self._get_links(sub)
            xls_files = [l for l in links if self._is_excel_file(l)]
            
            if not xls_files:
                self._log(f"Нет Excel файлов в {sub_name}")
                continue
            
            for url in xls_files:
                self._download_file(url)
        
        self._save_manifest()
        self._log(f"\n{'=' * 70}")
        self._log(f"ЗАГРУЗКА ЗАВЕРШЕНА! Скачано новых файлов: {len(self.new_files)}")
        self._log("=" * 70)
        
        return len(self.new_files)


def main():
    """Главная функция"""
    import argparse
    
    parser = argparse.ArgumentParser(
        description='Скачивание Excel файлов с расписанием с сайта BTEU'
    )
    
    parser.add_argument(
        '--dir',
        type=str,
        default=r'D:\Excel file',
        help='Директория для сохранения файлов (по умолчанию: D:\\Excel file)'
    )
    
    args = parser.parse_args()
    
    print("=" * 70)
    print("СКАЧИВАНИЕ РАСПИСАНИЯ С САЙТА BTEU")
    print("=" * 70)
    print()
    
    downloader = ScheduleDownloader(args.dir)
    files_count = downloader.download_all()
    
    print()
    print(f"✓ Скачано файлов: {files_count}")
    print(f"✓ Файлы сохранены в: {args.dir}")
    print()
    
    if files_count > 0:
        print("Следующий шаг: обработайте файлы через batch_parser.py")
        print(f"  python backend/batch_parser.py --dir \"{args.dir}\"")
    else:
        print("Новых файлов не найдено. Все файлы уже скачаны.")


if __name__ == '__main__':
    main()

