#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Скрипт для определения кодировки DBF файлов

Использование:
    python detect_encoding.py <путь_к_файлу>

Пример:
    python detect_encoding.py /path/to/file.dbf
    python detect_encoding.py C:\Users\danwi\AppData\Local\Temp\_tc\surok.dbf
"""

import sys
import os

try:
    import chardet
except ImportError:
    print("Ошибка: библиотека chardet не установлена.")
    print("Установите её командой: pip install chardet")
    sys.exit(1)


def detect_file_encoding(file_path):
    """
    Определяет кодировку файла
    """
    if not os.path.exists(file_path):
        print(f"Ошибка: файл '{file_path}' не найден")
        return None
    
    try:
        with open(file_path, 'rb') as file:
            raw_data = file.read()
            
            # Используем chardet для определения кодировки
            result = chardet.detect(raw_data)
            
            print(f"\n{'='*60}")
            print(f"Файл: {file_path}")
            print(f"Размер: {len(raw_data)} байт")
            print(f"{'='*60}")
            print(f"Определенная кодировка: {result['encoding']}")
            print(f"Уверенность: {result['confidence'] * 100:.2f}%")
            print(f"Язык: {result.get('language', 'N/A')}")
            
            # Попробуем прочитать первые строки в разных кодировках
            print(f"\n{'Попытка чтения в разных кодировках:':-^60}")
            
            encodings_to_try = [
                ('windows-1251', 'Windows-1251 (CP1251) - стандарт для Windows'),
                ('cp866', 'CP866 - DOS кодировка'),
                ('utf-8', 'UTF-8 - современный стандарт'),
                ('koi8-r', 'KOI8-R - старые UNIX системы'),
                ('iso-8859-5', 'ISO-8859-5 - альтернативная кириллица')
            ]
            
            # Берем первые 500 байт для тестирования
            sample_size = min(500, len(raw_data))
            sample_data = raw_data[:sample_size]
            
            best_encoding = None
            best_text = None
            best_score = 0
            
            for enc, description in encodings_to_try:
                try:
                    decoded = sample_data.decode(enc, errors='ignore')
                    
                    # Подсчитываем русские буквы
                    russian_chars = sum(1 for c in decoded if 'А' <= c <= 'я' or c in 'Ёё')
                    total_chars = len(decoded)
                    
                    # Подсчитываем мусорные символы
                    garbage_chars = decoded.count('?') + decoded.count('�')
                    
                    if total_chars > 0:
                        russian_ratio = (russian_chars / total_chars) * 100
                        garbage_ratio = (garbage_chars / total_chars) * 100
                        
                        # Вычисляем балл качества
                        score = russian_ratio - (garbage_ratio * 2)  # Штраф за мусор
                        
                        status = "✓" if score > 20 and garbage_ratio < 5 else "✗"
                        print(f"\n{status} {enc:15} ({description})")
                        print(f"   Русских букв: {russian_chars}/{total_chars} ({russian_ratio:.1f}%)")
                        print(f"   Мусорных символов: {garbage_chars} ({garbage_ratio:.1f}%)")
                        print(f"   Балл качества: {score:.1f}")
                        print(f"   Пример: {decoded[:80].replace(chr(0), ' ').strip()}")
                        
                        if score > best_score and garbage_ratio < 10:
                            best_score = score
                            best_encoding = enc
                            best_text = decoded
                    else:
                        print(f"\n✗ {enc:15} - пустой результат")
                        
                except Exception as e:
                    print(f"\n✗ {enc:15} - ошибка декодирования: {e}")
            
            print(f"\n{'='*60}")
            if best_encoding:
                print(f"РЕКОМЕНДУЕМАЯ КОДИРОВКА: {best_encoding}")
                print(f"Балл качества: {best_score:.1f}")
                print(f"\nПример текста:")
                print(f"{best_text[:200].replace(chr(0), ' ').strip()}")
            else:
                print("Не удалось определить оптимальную кодировку")
                print("Попробуйте проанализировать файл вручную")
            print(f"{'='*60}\n")
            
            return best_encoding or result['encoding']
            
    except Exception as e:
        print(f"Ошибка: {e}")
        import traceback
        traceback.print_exc()
        return None


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Использование: python detect_encoding.py <путь_к_файлу>")
        print("\nПример:")
        print("  python detect_encoding.py /path/to/file.dbf")
        print("  python detect_encoding.py C:\\Users\\danwi\\AppData\\Local\\Temp\\_tc\\surok.dbf")
        print("\nСкрипт определит кодировку DBF файла и покажет примеры")
        print("декодирования в разных кодировках.")
        sys.exit(1)
    
    file_path = sys.argv[1]
    detected_encoding = detect_file_encoding(file_path)
    
    if detected_encoding:
        print(f"\n✓ Рекомендуется использовать кодировку: {detected_encoding}")
        sys.exit(0)
    else:
        print("\n✗ Не удалось определить кодировку")
        sys.exit(1)

