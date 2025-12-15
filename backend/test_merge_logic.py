"""Тест логики объединения"""
import sys
import io

# Исправление кодировки для Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# Тестовые данные
accumulated = "2-22   МЕТРОЛОГИЯ, СТАНДАРТИЗАЦИЯ И СЕРТИФИКАЦИЯ (В ИНФОРМАЦИОННЫХ"
cell_str = "                 ТЕХНОЛОГИЯХ) доц.Авдашкова Л.П."

print("Тест логики объединения:")
print(f"Накопленное: '{accumulated}'")
print(f"Текущая ячейка: '{cell_str}'")
print()

cell_upper = cell_str.strip().upper()
prev_upper = accumulated.strip().upper()

print(f"cell_upper: '{cell_upper}'")
print(f"prev_upper: '{prev_upper}'")
print()

cell_clean = cell_upper.replace('"', '').replace("'", '').strip()
prev_clean = prev_upper.replace('"', '').replace("'", '').strip()

print(f"cell_clean: '{cell_clean}'")
print(f"prev_clean: '{prev_clean}'")
print()

# Проверяем условия
cond1 = cell_clean.startswith('ТЕХНОЛОГИЯХ') or cell_clean.startswith('ТЕХНОЛОГИЯ') or (cell_clean.endswith(')') and len(cell_clean) < 20)
cond2 = prev_clean.endswith('(В ИНФОРМАЦИОННЫХ') or prev_clean.endswith('(В ИНФОРМАЦИОННЫХ ') or prev_clean.endswith('ИНФОРМАЦИОННЫХ') or prev_clean.endswith('ИНФОРМАЦИОННЫХ ') or '(В ИНФОРМАЦИОННЫХ' in prev_clean

print(f"Условие 1 (cell начинается с ТЕХНОЛОГИЯХ или заканчивается на )): {cond1}")
print(f"  - startswith ТЕХНОЛОГИЯХ: {cell_clean.startswith('ТЕХНОЛОГИЯХ')}")
print(f"  - startswith ТЕХНОЛОГИЯ: {cell_clean.startswith('ТЕХНОЛОГИЯ')}")
print(f"  - endswith ) и len < 20: {cell_clean.endswith(')') and len(cell_clean) < 20} (len={len(cell_clean)})")
print()

print(f"Условие 2 (prev содержит ИНФОРМАЦИОННЫХ): {cond2}")
print(f"  - endswith '(В ИНФОРМАЦИОННЫХ': {prev_clean.endswith('(В ИНФОРМАЦИОННЫХ')}")
print(f"  - endswith '(В ИНФОРМАЦИОННЫХ ': {prev_clean.endswith('(В ИНФОРМАЦИОННЫХ ')}")
print(f"  - endswith 'ИНФОРМАЦИОННЫХ': {prev_clean.endswith('ИНФОРМАЦИОННЫХ')}")
print(f"  - endswith 'ИНФОРМАЦИОННЫХ ': {prev_clean.endswith('ИНФОРМАЦИОННЫХ ')}")
print(f"  - '(В ИНФОРМАЦИОННЫХ' in prev: {'(В ИНФОРМАЦИОННЫХ' in prev_clean}")
print()

is_explicit = cond1 and cond2
print(f"is_explicit_continuation: {is_explicit}")

