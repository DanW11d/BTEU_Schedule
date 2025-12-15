#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Скрипт для скачивания медиа через yt-dlp
Использование: python download_media.py <url> [output_dir] [format]
"""

import sys
import json
import subprocess
import os
from pathlib import Path

def download_media(url, output_dir="C:/temp", format_type="best"):
    """
    Скачивает медиа через yt-dlp
    
    Args:
        url: URL для скачивания
        output_dir: Директория для сохранения
        format_type: Формат (best, video, audio)
    
    Returns:
        dict с результатом
    """
    try:
        # Создаем директорию если не существует
        Path(output_dir).mkdir(parents=True, exist_ok=True)
        
        # Определяем формат в зависимости от типа
        if format_type == "video":
            format_str = "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best"
        elif format_type == "audio":
            format_str = "bestaudio[ext=m4a]/bestaudio/best"
        else:
            format_str = "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best"
        
        # Команда yt-dlp
        # --print after_move:filepath - выводит путь к файлу после скачивания
        cmd = [
            'yt-dlp',
            '--format', format_str,
            '--output', f'{output_dir}/%(title)s.%(ext)s',
            '--print', 'after_move:filepath',
            '--no-warnings',
            '--quiet',
            url
        ]
        
        # Выполняем команду
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=300,
            encoding='utf-8',
            errors='ignore'
        )
        
        if result.returncode == 0:
            # Извлекаем путь к файлу из вывода
            file_path = result.stdout.strip()
            
            # Убираем лишние символы
            file_path = file_path.replace('\n', '').replace('\r', '')
            
            if file_path and os.path.exists(file_path):
                # Определяем тип файла
                extension = os.path.splitext(file_path)[1].lower()
                is_video = extension in ['.mp4', '.webm', '.mov', '.avi', '.mkv', '.flv', '.m4v']
                is_audio = extension in ['.mp3', '.m4a', '.ogg', '.wav', '.flac', '.aac']
                
                content_type = 'video'
                if is_audio and not is_video:
                    content_type = 'audio'
                
                return {
                    'success': True,
                    'file_path': file_path,
                    'content_type': content_type,
                    'extension': extension,
                    'message': 'Файл успешно скачан'
                }
            else:
                return {
                    'success': False,
                    'error': 'Файл не найден после скачивания',
                    'stdout': result.stdout[:200],
                    'stderr': result.stderr[:200]
                }
        else:
            error_msg = result.stderr or result.stdout or 'Ошибка при скачивании'
            return {
                'success': False,
                'error': error_msg[:500],
                'exit_code': result.returncode
            }
            
    except subprocess.TimeoutExpired:
        return {
            'success': False,
            'error': 'Превышено время ожидания (5 минут)'
        }
    except Exception as e:
        return {
            'success': False,
            'error': str(e)
        }

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print(json.dumps({
            'success': False,
            'error': 'URL не указан. Использование: python download_media.py <url> [output_dir] [format]'
        }))
        sys.exit(1)
    
    url = sys.argv[1]
    output_dir = sys.argv[2] if len(sys.argv) > 2 else "C:/temp"
    format_type = sys.argv[3] if len(sys.argv) > 3 else "best"
    
    result = download_media(url, output_dir, format_type)
    print(json.dumps(result, ensure_ascii=False))

