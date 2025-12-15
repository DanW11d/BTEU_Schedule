"""
Модуль для интеграции с 1С:УАТ (Управление автотранспортом)
Создание заявок на перевозку через REST API
"""

import requests
import base64
import json
import logging
from typing import Dict, Optional, Any
from datetime import datetime

logger = logging.getLogger(__name__)


class UATIntegration:
    """Интеграция с 1С:УАТ через REST API"""
    
    def __init__(self, config: Dict):
        """
        Инициализация интеграции с 1С:УАТ
        
        Args:
            config: Словарь с настройками:
                - base_url: базовый URL API (например, 'http://server-uat/ut')
                - username: имя пользователя для аутентификации
                - password: пароль
                - api_path: путь к API (например, '/hs/TransportAPI')
                - auth_type: тип аутентификации ('basic' или 'token')
                - token: токен для аутентификации (если auth_type='token')
                - timeout: таймаут запросов в секундах (по умолчанию 30)
        """
        self.base_url = config.get('base_url', '').rstrip('/')
        self.username = config.get('username', '')
        self.password = config.get('password', '')
        self.api_path = config.get('api_path', '/hs/TransportAPI')
        self.auth_type = config.get('auth_type', 'basic')
        self.token = config.get('token', None)
        self.timeout = config.get('timeout', 30)
        
        # Формируем полный URL
        self.api_url = f"{self.base_url}{self.api_path}"
        
        # Подготавливаем заголовки аутентификации
        self.headers = self._prepare_headers()
    
    def _prepare_headers(self) -> Dict[str, str]:
        """Подготовка заголовков с аутентификацией"""
        headers = {'Content-Type': 'application/json'}
        
        if self.auth_type == 'basic':
            credentials = base64.b64encode(
                f"{self.username}:{self.password}".encode()
            ).decode()
            headers['Authorization'] = f'Basic {credentials}'
        elif self.auth_type == 'token' and self.token:
            headers['Authorization'] = f'Bearer {self.token}'
        
        return headers
    
    def test_connection(self) -> bool:
        """Проверка подключения к API 1С:УАТ"""
        try:
            # Пробуем простой GET запрос (если есть endpoint для проверки)
            test_url = f"{self.api_url}/health"  # или другой endpoint
            response = requests.get(
                test_url,
                headers=self.headers,
                timeout=self.timeout
            )
            
            if response.status_code in [200, 401, 403]:
                # Даже если 401/403, значит сервер доступен
                logger.info(f"Подключение к 1С:УАТ успешно (код: {response.status_code})")
                return True
            else:
                logger.warning(f"Неожиданный код ответа: {response.status_code}")
                return False
                
        except requests.exceptions.RequestException as e:
            logger.error(f"Ошибка подключения к 1С:УАТ: {e}")
            return False
    
    def create_transport_order(self, order_data: Dict) -> Optional[Dict]:
        """
        Создание заявки на перевозку в 1С:УАТ
        
        Args:
            order_data: Словарь с данными заявки в формате:
                {
                    "request_id": "EMAIL-001",
                    "datetime": "2025-11-20T10:00:00",
                    "from_location": "Склад №1",
                    "to_location": "Офис продаж",
                    "pickup_address": "Полный адрес забора",
                    "delivery_address": "Полный адрес доставки",
                    "pickup_date": "2025-11-20",
                    "pickup_time": "10:00",
                    "delivery_date": "2025-11-20",
                    "delivery_time": "14:00",
                    "cargo_name": "Мебель",
                    "cargo_weight": "20 тонн",
                    "cargo_volume": "1 м³",
                    "vehicle_type": "Грузовой",
                    "body_type": "Тент",
                    "loading_type": "задняя",
                    "contact_person": "Иванов Иван",
                    "contact_phone": "+79991234567",
                    "driver": "Наумов Валерий Станиславович",
                    "car": "Ford F MAX",
                    "car_number": "A992X0750",
                    "trailer_number": "EK603650",
                    "body_state": "Сухой, чистый",
                    "payment": "Безналичный расчет",
                    "documents": "акт выполненных работ"
                }
        
        Returns:
            Словарь с результатом создания заявки или None при ошибке
        """
        try:
            # Преобразуем данные в формат, ожидаемый API 1С:УАТ
            payload = self._format_order_data(order_data)
            
            url = f"{self.api_url}/orders"
            
            logger.info(f"Отправка заявки в 1С:УАТ: {url}")
            logger.debug(f"Данные заявки: {json.dumps(payload, ensure_ascii=False, indent=2)}")
            
            response = requests.post(
                url,
                json=payload,
                headers=self.headers,
                timeout=self.timeout
            )
            
            if response.status_code in [200, 201]:
                result = response.json()
                logger.info(f"Заявка успешно создана в 1С:УАТ: {result}")
                return result
            else:
                error_msg = f"Ошибка API 1С:УАТ: {response.status_code} - {response.text}"
                logger.error(error_msg)
                return {
                    'success': False,
                    'error': error_msg,
                    'status_code': response.status_code
                }
                
        except requests.exceptions.Timeout:
            error_msg = "Таймаут при обращении к API 1С:УАТ"
            logger.error(error_msg)
            return {'success': False, 'error': error_msg}
        except requests.exceptions.RequestException as e:
            error_msg = f"Ошибка соединения с 1С:УАТ: {e}"
            logger.error(error_msg)
            return {'success': False, 'error': error_msg}
        except Exception as e:
            error_msg = f"Неожиданная ошибка: {e}"
            logger.error(error_msg, exc_info=True)
            return {'success': False, 'error': error_msg}
    
    def _format_order_data(self, order_data: Dict) -> Dict:
        """
        Форматирование данных заявки в формат, ожидаемый API 1С:УАТ
        
        Структура может отличаться в зависимости от вашей конфигурации 1С.
        Этот метод нужно адаптировать под конкретную структуру API.
        """
        # Базовая структура для 1С:УАТ
        # Адаптируйте под вашу конфигурацию!
        payload = {
            "НомерЗаявки": order_data.get('request_id', ''),
            "ДатаВремя": order_data.get('datetime', datetime.now().isoformat()),
            "Маршрут": {
                "ПунктОтправления": order_data.get('from_location', ''),
                "ПунктНазначения": order_data.get('to_location', ''),
                "АдресОтправления": order_data.get('pickup_address', ''),
                "АдресНазначения": order_data.get('delivery_address', '')
            },
            "График": {
                "ОтправлениеС": f"{order_data.get('pickup_date', '')}T{order_data.get('pickup_time', '00:00')}:00",
                "ОтправлениеПо": f"{order_data.get('pickup_date', '')}T{order_data.get('pickup_time', '00:00')}:00",
                "ПрибытиеС": f"{order_data.get('delivery_date', '')}T{order_data.get('delivery_time', '00:00')}:00",
                "ПрибытиеПо": f"{order_data.get('delivery_date', '')}T{order_data.get('delivery_time', '00:00')}:00"
            },
            "Груз": {
                "Наименование": order_data.get('cargo_name', ''),
                "Вес": order_data.get('cargo_weight', ''),
                "Объем": order_data.get('cargo_volume', ''),
                "ТипКузова": order_data.get('body_type', ''),
                "ТипПогрузки": order_data.get('loading_type', '')
            },
            "Транспорт": {
                "ТипТранспорта": order_data.get('vehicle_type', 'Автомобильная грузовая'),
                "Автомобиль": order_data.get('car', ''),
                "ГосНомерАвтомобиля": order_data.get('car_number', ''),
                "ГосНомерПрицепа": order_data.get('trailer_number', ''),
                "Водитель": order_data.get('driver', '')
            },
            "Контакты": {
                "КонтактноеЛицо": order_data.get('contact_person', ''),
                "Телефон": order_data.get('contact_phone', '')
            },
            "Условия": {
                "СостояниеКузова": order_data.get('body_state', ''),
                "Оплата": order_data.get('payment', ''),
                "Документы": order_data.get('documents', '')
            }
        }
        
        return payload
    
    def get_order_status(self, order_id: str) -> Optional[Dict]:
        """Получить статус заявки по ID"""
        try:
            url = f"{self.api_url}/orders/{order_id}"
            response = requests.get(url, headers=self.headers, timeout=self.timeout)
            
            if response.status_code == 200:
                return response.json()
            else:
                logger.warning(f"Не удалось получить статус заявки {order_id}: {response.status_code}")
                return None
        except Exception as e:
            logger.error(f"Ошибка при получении статуса заявки: {e}")
            return None
    
    def get_driver_task(self, order_id: str) -> Optional[Dict]:
        """Получить сформированное задание для водителя"""
        try:
            url = f"{self.api_url}/orders/{order_id}/driver_task"
            response = requests.get(url, headers=self.headers, timeout=self.timeout)
            
            if response.status_code == 200:
                return response.json()
            else:
                logger.warning(f"Не удалось получить задание водителя для {order_id}: {response.status_code}")
                return None
        except Exception as e:
            logger.error(f"Ошибка при получении задания водителя: {e}")
            return None


# Пример использования
if __name__ == "__main__":
    # Настройка логирования
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # Пример конфигурации
    config = {
        'base_url': 'http://uat-server/ut',
        'username': 'api_user',
        'password': 'api_password',
        'api_path': '/hs/TransportAPI',
        'auth_type': 'basic',
        'timeout': 30
    }
    
    uat = UATIntegration(config)
    
    # Проверка подключения
    if uat.test_connection():
        print("Подключение к 1С:УАТ успешно!")
        
        # Пример создания заявки
        order_data = {
            'request_id': 'EMAIL-001',
            'datetime': '2025-11-20T10:00:00',
            'from_location': 'Склад №1',
            'to_location': 'Офис продаж',
            'pickup_address': 'Московская область, г.о. Люберцы, ул. Ленина, д. 93',
            'delivery_address': 'Московская область, р.п. Обухово, дом 36',
            'pickup_date': '2025-11-20',
            'pickup_time': '10:00',
            'delivery_date': '2025-11-20',
            'delivery_time': '14:00',
            'cargo_name': 'Мебель',
            'cargo_weight': '20 тонн',
            'cargo_volume': '1 м³',
            'body_type': 'Тент',
            'loading_type': 'задняя',
            'contact_person': 'Иванов Иван',
            'contact_phone': '+79991234567',
            'driver': 'Наумов Валерий Станиславович',
            'car': 'Ford F MAX',
            'car_number': 'A992X0750'
        }
        
        result = uat.create_transport_order(order_data)
        print(f"Результат: {result}")
    else:
        print("Ошибка подключения к 1С:УАТ")

