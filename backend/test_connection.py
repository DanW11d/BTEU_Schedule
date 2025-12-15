"""
Скрипт для тестирования подключения к БД АРМА PostgreSQL
"""

import logging
from db_connection import get_connector

# Настройка логирования
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)


def main():
    """Основная функция тестирования"""
    
    print("=" * 60)
    print("ТЕСТИРОВАНИЕ ПОДКЛЮЧЕНИЯ К БД АРМА PostgreSQL")
    print("=" * 60)
    
    # Получение коннектора
    connector = get_connector()
    
    # Тест подключения
    print("\n1. Тест подключения к БД...")
    if connector.test_connection():
        print("   ✅ Подключение успешно!")
    else:
        print("   ❌ Ошибка подключения!")
        return
    
    # Получение списка таблиц
    print("\n2. Получение списка таблиц...")
    try:
        tables = connector.get_table_list()
        print(f"   ✅ Найдено таблиц: {len(tables)}")
        
        if tables:
            print("\n   Список таблиц:")
            for i, table in enumerate(tables[:20], 1):  # Показать первые 20
                print(f"   {i:2d}. {table}")
            
            if len(tables) > 20:
                print(f"   ... и еще {len(tables) - 20} таблиц")
    except Exception as e:
        print(f"   ❌ Ошибка: {e}")
        return
    
    # Анализ ключевых таблиц
    print("\n3. Анализ ключевых таблиц расписания...")
    
    key_tables = {
        'tmp_graf': 'Расписание дневного обучения (по дням)',
        'frasp': 'Расписание дневного обучения (схема)',
        'fraspz': 'Расписание заочного обучения',
        'fgrafikz': 'График заочного обучения (занятия + экзамены)',
        'sgrupp': 'Справочник групп',
        'spred': 'Справочник предметов',
        'sprep': 'Справочник преподавателей',
        'saudit': 'Справочник аудиторий',
        'fvzan': 'Справочник видов занятий',
        'sfak': 'Справочник факультетов',
        'ssesg1': 'Периоды сессий',
    }
    
    found_tables = {}
    for table_name, description in key_tables.items():
        # Проверить разные варианты написания (регистр)
        table_found = None
        for t in tables:
            if t.lower() == table_name.lower():
                table_found = t
                break
        
        if table_found:
            found_tables[table_found] = description
            print(f"   ✅ {table_found} - {description}")
            
            # Показать структуру
            try:
                columns = connector.get_table_columns(table_found)
                print(f"      Колонок: {len(columns)}")
                if columns:
                    print(f"      Примеры колонок: {', '.join([c['column_name'] for c in columns[:5]])}")
            except Exception as e:
                print(f"      ⚠️  Не удалось получить структуру: {e}")
        else:
            print(f"   ❌ {table_name} - НЕ НАЙДЕНА")
    
    # Тест получения данных
    print("\n4. Тест получения данных...")
    if found_tables:
        first_table = list(found_tables.keys())[0]
        try:
            sample = connector.get_table_sample(first_table, limit=3)
            print(f"   ✅ Данные из '{first_table}':")
            for i, row in enumerate(sample, 1):
                print(f"      Запись {i}: {dict(list(row.items())[:5])}")  # Первые 5 полей
        except Exception as e:
            print(f"   ⚠️  Ошибка получения данных: {e}")
    
    # Закрытие соединения
    print("\n5. Закрытие соединения...")
    connector.close()
    print("   ✅ Соединение закрыто")
    
    print("\n" + "=" * 60)
    print("ТЕСТИРОВАНИЕ ЗАВЕРШЕНО")
    print("=" * 60)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\nПрервано пользователем")
    except Exception as e:
        logger.exception("Критическая ошибка:")
        print(f"\n❌ Критическая ошибка: {e}")

