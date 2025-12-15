"""
Утилиты для обработки имен файлов с расписанием
"""
import re
from typing import Optional, Tuple


# Маппинг латинских букв на кириллические коды групп
LATIN_TO_CYRILLIC = {
    'p': 'П', 'e': 'Э', 'i': 'И', 'm': 'М', 'a': 'А',
    'z': 'З', 'b': 'Б', 'f': 'Ф', 'g': 'Г', 'l': 'Л',
    'r': 'Р', 's': 'С', 'c': 'Ц', 't': 'Т', 'k': 'К',
    'yu': 'Ю', 'sh': 'Ш', 'ch': 'Ч', 'zh': 'Ж'
}

# Двухбуквенные коды (приоритет выше)
TWO_LETTER_CODES = ['yu', 'sh', 'ch', 'zh', 'es', 'is', 'ms', 'ps', 'bs', 'ks', 'ls', 'rs', 'ts', 'gc']


def extract_group_code(filename: str) -> Optional[str]:
    """
    Извлекает код группы из имени файла
    
    Примеры:
    - "p-1 (28.08.25).xlsx" → "П-1"
    - "e-2-s-15.09.25 (12.09.25).xls" → "Э-2"
    - "i-3 (29.08.25).xls" → "И-3"
    - "es-11z-s-10.11.25-po-29.11.25 (04.11.25).xlsx" → "ЭС-11з"
    - "p-2-s-15.09.25 (12.09.25).xls" → "П-2"
    
    Args:
        filename: Имя файла
        
    Returns:
        Код группы в формате "П-1", "Э-2" и т.д., или None если не удалось извлечь
    """
    # Убираем расширение
    name = filename.rsplit('.', 1)[0].lower()
    
    # Убираем дату в скобках: "p-1 (28.08.25)" → "p-1"
    name = re.sub(r'\s*\(\d{2}\.\d{2}\.\d{2}\)\s*$', '', name)
    
    # Убираем суффиксы типа "-s-15.09.25", "-po-29.11.25"
    name = re.sub(r'-s-\d{2}\.\d{2}\.\d{2}', '', name)
    name = re.sub(r'-po-\d{2}\.\d{2}\.\d{2}', '', name)
    name = re.sub(r'-nachitka', '', name)
    name = re.sub(r'-zashhita.*', '', name)
    
    # Извлекаем код группы
    # Паттерн: буквы + дефис + цифры + опционально буква (для заочной формы)
    match = re.match(r'^([a-z]+)-(\d+)([a-z]?)(?:-|$)', name)
    if not match:
        # Пробуем без дефиса: "p1" → "П-1"
        match = re.match(r'^([a-z]+)(\d+)([a-z]?)(?:-|$)', name)
    
    if match:
        prefix = match.group(1)
        number = match.group(2)
        suffix = match.group(3) if len(match.groups()) > 2 else ''
        
        # Преобразуем префикс в кириллицу
        cyrillic_prefix = _convert_prefix_to_cyrillic(prefix)
        
        if cyrillic_prefix:
            # Преобразуем суффикс (например, "z" → "з" для заочной формы)
            cyrillic_suffix = ''
            if suffix:
                suffix_map = {'z': 'з', 'o': 'о', 'd': 'д'}
                cyrillic_suffix = suffix_map.get(suffix.lower(), suffix)
            
            if cyrillic_suffix:
                return f"{cyrillic_prefix}-{number}{cyrillic_suffix}"
            return f"{cyrillic_prefix}-{number}"
    
    return None


def _convert_prefix_to_cyrillic(prefix: str) -> Optional[str]:
    """
    Преобразует латинский префикс в кириллический код группы
    
    Примеры:
    - "p" → "П"
    - "es" → "ЭС"
    - "yu" → "Ю"
    """
    prefix_lower = prefix.lower()
    
    # Сначала проверяем двухбуквенные коды
    for two_letter in TWO_LETTER_CODES:
        if prefix_lower.startswith(two_letter):
            # Преобразуем каждую букву
            result = ''
            remaining = prefix_lower
            while remaining:
                matched = False
                for code in TWO_LETTER_CODES:
                    if remaining.startswith(code):
                        # Для составных кодов типа "es", "is" - преобразуем каждую букву
                        if code in ['es', 'is', 'ms', 'ps', 'bs', 'ks', 'ls', 'rs', 'ts']:
                            result += LATIN_TO_CYRILLIC.get(code[0], code[0]).upper()
                            result += LATIN_TO_CYRILLIC.get(code[1], code[1]).upper()
                        elif code == 'gc':
                            result += 'ГЦ'
                        else:
                            result += LATIN_TO_CYRILLIC.get(code, code).upper()
                        remaining = remaining[len(code):]
                        matched = True
                        break
                if not matched:
                    # Обрабатываем оставшиеся буквы по одной
                    if remaining[0] in LATIN_TO_CYRILLIC:
                        result += LATIN_TO_CYRILLIC[remaining[0]].upper()
                    else:
                        result += remaining[0].upper()
                    remaining = remaining[1:]
            return result
    
    # Если не нашли двухбуквенный код, обрабатываем по одной букве
    result = ''
    for char in prefix_lower:
        if char in LATIN_TO_CYRILLIC:
            result += LATIN_TO_CYRILLIC[char].upper()
        else:
            result += char.upper()
    
    return result if result else None


def parse_filename(filename: str) -> Tuple[Optional[str], Optional[str]]:
    """
    Парсит имя файла и извлекает код группы и дату модификации
    
    Args:
        filename: Имя файла
        
    Returns:
        Кортеж (group_code, modification_date)
        Пример: ("П-1", "28.08.25")
    """
    # Извлекаем дату
    date_match = re.search(r'\((\d{2}\.\d{2}\.\d{2})\)', filename)
    modification_date = date_match.group(1) if date_match else None
    
    # Извлекаем код группы
    group_code = extract_group_code(filename)
    
    return group_code, modification_date


# Тесты
if __name__ == '__main__':
    test_cases = [
        "p-1 (28.08.25).xlsx",
        "e-2 (28.08.25).xlsx",
        "i-3 (29.08.25).xls",
        "p-2-s-15.09.25 (12.09.25).xls",
        "es-11z-s-10.11.25-po-29.11.25 (04.11.25).xlsx",
        "e-51z-s-24.11.25-po-08.12.25 (14.11.25).xlsx",
        "ls-111213-s-12.11.25-po-28.11.25-1 (04.11.25).xlsx",
        "gc-41z-s-29.11.25-zashhita-otchetov-po-praktike (14.11.25).xlsx",
        "yu-11-25-s-27.10.25-po-24.11.25 (17.10.25).xlsx",
    ]
    
    import sys
    import io
    if sys.platform == 'win32':
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    
    print("Тестирование извлечения кода группы:\n")
    for filename in test_cases:
        group_code, date = parse_filename(filename)
        print(f"{filename:60} -> {group_code} ({date})")

