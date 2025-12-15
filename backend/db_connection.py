"""
Модуль для подключения к БД PostgreSQL университета через VPN
"""

import psycopg2
from psycopg2 import pool
from psycopg2.extras import RealDictCursor
import os
from typing import Optional, Dict, List, Any
from contextlib import contextmanager
import logging

# Попытка загрузить dotenv (опционально)
try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    # dotenv не установлен, используем значения по умолчанию
    pass

logger = logging.getLogger(__name__)


class ARMAPostgreSQLConnector:
    """
    Коннектор для подключения к БД АРМА PostgreSQL
    """
    
    def __init__(
        self,
        host: Optional[str] = None,
        port: int = 5432,
        database: str = "schedule_bot",
        user: str = "dan",
        password: str = "7631",
        min_connections: int = 1,
        max_connections: int = 5
    ):
        """
        Инициализация коннектора
        
        Args:
            host: IP адрес сервера БД (по умолчанию из переменных окружения или 192.168.0.232)
            port: Порт PostgreSQL (по умолчанию 5432)
            database: Имя базы данных
            user: Имя пользователя
            password: Пароль
            min_connections: Минимальное количество соединений в пуле
            max_connections: Максимальное количество соединений в пуле
        """
        self.host = host or os.getenv("ARMA_DB_HOST", "192.168.0.232")
        self.port = port
        self.database = database
        self.user = user
        self.password = password
        
        # Строка подключения
        self.connection_string = (
            f"host={host} "
            f"port={port} "
            f"dbname={database} "
            f"user={user} "
            f"password={password}"
        )
        
        # Пул соединений
        self.connection_pool: Optional[pool.ThreadedConnectionPool] = None
        self._init_connection_pool(min_connections, max_connections)
    
    def _init_connection_pool(self, min_conn: int, max_conn: int):
        """Инициализация пула соединений"""
        try:
            self.connection_pool = pool.ThreadedConnectionPool(
                min_conn,
                max_conn,
                self.connection_string
            )
            logger.info("Пул соединений инициализирован успешно")
        except Exception as e:
            logger.error(f"Ошибка инициализации пула соединений: {e}")
            raise
    
    @contextmanager
    def get_connection(self):
        """
        Контекстный менеджер для получения соединения из пула
        
        Usage:
            with connector.get_connection() as conn:
                cursor = conn.cursor()
                cursor.execute("SELECT * FROM table")
        """
        conn = None
        try:
            conn = self.connection_pool.getconn()
            yield conn
        except Exception as e:
            if conn:
                conn.rollback()
            logger.error(f"Ошибка при работе с БД: {e}")
            raise
        finally:
            if conn:
                self.connection_pool.putconn(conn)
    
    def test_connection(self) -> bool:
        """
        Тест подключения к БД
        
        Returns:
            True если подключение успешно, False иначе
        """
        try:
            with self.get_connection() as conn:
                cursor = conn.cursor()
                cursor.execute("SELECT version();")
                version = cursor.fetchone()
                logger.info(f"Подключение успешно. PostgreSQL версия: {version[0]}")
                return True
        except Exception as e:
            logger.error(f"Ошибка подключения к БД: {e}")
            return False
    
    def execute_query(self, query: str, params: Optional[tuple] = None) -> List[Dict[str, Any]]:
        """
        Выполнение SELECT запроса
        
        Args:
            query: SQL запрос
            params: Параметры запроса (для защиты от SQL injection)
        
        Returns:
            Список словарей с результатами
        """
        try:
            with self.get_connection() as conn:
                cursor = conn.cursor(cursor_factory=RealDictCursor)
                cursor.execute(query, params)
                results = cursor.fetchall()
                return [dict(row) for row in results]
        except Exception as e:
            logger.error(f"Ошибка выполнения запроса: {e}")
            logger.error(f"Запрос: {query}")
            raise
    
    def execute_update(self, query: str, params: Optional[tuple] = None) -> int:
        """
        Выполнение INSERT/UPDATE/DELETE запроса
        
        Args:
            query: SQL запрос
            params: Параметры запроса
        
        Returns:
            Количество затронутых строк
        """
        try:
            with self.get_connection() as conn:
                cursor = conn.cursor()
                cursor.execute(query, params)
                conn.commit()
                return cursor.rowcount
        except Exception as e:
            logger.error(f"Ошибка выполнения обновления: {e}")
            logger.error(f"Запрос: {query}")
            raise
    
    def get_table_list(self) -> List[str]:
        """
        Получение списка таблиц в БД
        
        Returns:
            Список имен таблиц
        """
        query = """
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_schema = 'public'
            ORDER BY table_name;
        """
        results = self.execute_query(query)
        return [row['table_name'] for row in results]
    
    def get_table_columns(self, table_name: str) -> List[Dict[str, Any]]:
        """
        Получение информации о колонках таблицы
        
        Args:
            table_name: Имя таблицы
        
        Returns:
            Список словарей с информацией о колонках
        """
        query = """
            SELECT 
                column_name,
                data_type,
                character_maximum_length,
                is_nullable,
                column_default
            FROM information_schema.columns
            WHERE table_schema = 'public' 
            AND table_name = %s
            ORDER BY ordinal_position;
        """
        return self.execute_query(query, (table_name,))
    
    def get_table_sample(self, table_name: str, limit: int = 10) -> List[Dict[str, Any]]:
        """
        Получение образца данных из таблицы
        
        Args:
            table_name: Имя таблицы
            limit: Количество записей
        
        Returns:
            Список записей
        """
        query = f'SELECT * FROM "{table_name}" LIMIT %s;'
        return self.execute_query(query, (limit,))
    
    def close(self):
        """Закрытие пула соединений"""
        if self.connection_pool:
            self.connection_pool.closeall()
            logger.info("Пул соединений закрыт")


# Глобальный экземпляр коннектора
_connector: Optional[ARMAPostgreSQLConnector] = None


def get_connector() -> ARMAPostgreSQLConnector:
    """
    Получение глобального экземпляра коннектора (Singleton)
    
    Returns:
        Экземпляр ARMAPostgreSQLConnector
    """
    global _connector
    
    if _connector is None:
        _connector = ARMAPostgreSQLConnector(
            host=os.getenv("ARMA_DB_HOST", "192.168.0.232"),
            port=int(os.getenv("ARMA_DB_PORT", "5432")),
            database=os.getenv("ARMA_DB_NAME", "schedule_bot"),
            user=os.getenv("ARMA_DB_USER", "dan"),
            password=os.getenv("ARMA_DB_PASSWORD", "7631")
        )
    
    return _connector


if __name__ == "__main__":
    # Тестирование подключения
    import logging
    
    logging.basicConfig(level=logging.INFO)
    
    connector = get_connector()
    
    # Тест подключения
    if connector.test_connection():
        print("✅ Подключение к БД успешно!")
        
        # Получение списка таблиц
        print("\n📋 Список таблиц в БД:")
        tables = connector.get_table_list()
        for table in tables:
            print(f"  - {table}")
        
        # Пример: получение структуры таблицы (если есть)
        if tables:
            first_table = tables[0]
            print(f"\n📊 Структура таблицы '{first_table}':")
            columns = connector.get_table_columns(first_table)
            for col in columns:
                print(f"  - {col['column_name']}: {col['data_type']}")
            
            # Пример: получение образца данных
            print(f"\n📄 Образец данных из '{first_table}':")
            sample = connector.get_table_sample(first_table, limit=5)
            for row in sample:
                print(f"  {row}")
    
    connector.close()

