"""
Главный скрипт автоматизации обработки заявок на перевозку
Объединяет все модули: обработка email, парсинг, интеграция с 1С:УАТ, отправка водителю
"""

import logging
import sys
import time
from typing import Dict, Optional
from datetime import datetime
import json

from transport_email_handler import EmailHandler
from transport_request_parser import TransportRequestParser
from transport_1c_integration import UATIntegration
from transport_driver_notifier import DriverNotifier
from transport_request_detector import RequestDetector

# Настройка логирования
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('transport_automation.log', encoding='utf-8'),
        logging.StreamHandler(sys.stdout)
    ]
)

logger = logging.getLogger(__name__)


class TransportAutomation:
    """Главный класс для автоматизации обработки заявок"""
    
    def __init__(self, config: Dict):
        """
        Инициализация автоматизации
        
        Args:
            config: Полная конфигурация системы
        """
        self.config = config
        
        # Инициализация компонентов
        self.email_handler = EmailHandler(config.get('email', {}))
        self.parser = TransportRequestParser()
        self.detector = RequestDetector(config.get('request_detection', {}))
        self.uat_integration = UATIntegration(config.get('1c_uat', {}))
        self.driver_notifier = DriverNotifier(
            self.email_handler,
            config.get('driver_notification', {})
        )
        
        # Настройки обработки
        self.check_interval = config.get('check_interval', 300)  # 5 минут по умолчанию
        self.max_emails_per_check = config.get('max_emails_per_check', 10)
        self.auto_archive = config.get('auto_archive', True)
        self.notify_on_error = config.get('notify_on_error', True)
        self.admin_email = config.get('admin_email', None)
        
        # Настройки определения заявок
        self.min_confidence = config.get('min_confidence', 0.7)  # Минимальная уверенность для обработки
        self.skip_non_requests = config.get('skip_non_requests', True)  # Пропускать не-заявки
        
        logger.info("Автоматизация обработки заявок инициализирована")
    
    def process_email(self, email_info: Dict) -> bool:
        """
        Обработать одно письмо с заявкой
        
        Args:
            email_info: Информация о письме
        
        Returns:
            True если обработка успешна, False иначе
        """
        email_id = email_info.get('id')
        subject = email_info.get('subject', '')
        sender = email_info.get('sender', '')
        body = email_info.get('body', '')
        
        logger.info(f"Проверка письма: {subject} от {sender}")
        
        # Шаг 0: Определение, является ли письмо заявкой
        is_request, confidence, reason = self.detector.is_transport_request(email_info)
        
        logger.info(
            f"Анализ письма: заявка={is_request}, уверенность={confidence:.2%}, "
            f"причина={reason}"
        )
        
        if not is_request:
            if self.skip_non_requests:
                logger.info(f"Письмо не является заявкой (уверенность {confidence:.2%}), пропускаем")
                # Помечаем как прочитанное, но не обрабатываем
                self.email_handler.mark_as_read(email_id)
                return False
            else:
                logger.warning(
                    f"Письмо может не быть заявкой (уверенность {confidence:.2%}), "
                    f"но продолжаем обработку"
                )
        
        if confidence < self.min_confidence:
            logger.warning(
                f"Низкая уверенность определения заявки ({confidence:.2%} < {self.min_confidence:.2%})"
            )
        
        try:
            # Шаг 1: Парсинг заявки
            logger.info("Шаг 1: Парсинг заявки из текста письма...")
            request = self.parser.parse(body)
            
            # Валидация распарсенных данных
            if not self._validate_request(request):
                error_msg = "Не удалось извлечь обязательные данные из заявки"
                logger.error(error_msg)
                self._notify_error(email_info, error_msg)
                return False
            
            # Преобразуем в словарь
            request_dict = self.parser.to_dict(request)
            logger.info("Заявка успешно распарсена")
            
            # Шаг 2: Создание заявки в 1С:УАТ
            logger.info("Шаг 2: Создание заявки в 1С:УАТ...")
            order_data = self._prepare_order_data(request_dict, email_info)
            result = self.uat_integration.create_transport_order(order_data)
            
            if not result or not result.get('success', False):
                error_msg = f"Ошибка создания заявки в 1С:УАТ: {result.get('error', 'Неизвестная ошибка')}"
                logger.error(error_msg)
                self._notify_error(email_info, error_msg)
                return False
            
            order_id = result.get('order_number') or result.get('id') or email_id
            logger.info(f"Заявка создана в 1С:УАТ: {order_id}")
            
            # Шаг 3: Отправка задания водителю
            logger.info("Шаг 3: Отправка задания водителю...")
            driver_sent = self.driver_notifier.send_driver_task(request_dict, order_id)
            
            if not driver_sent:
                logger.warning("Не удалось отправить задание водителю, но заявка создана в системе")
            else:
                logger.info("Задание отправлено водителю")
            
            # Шаг 4: Помечаем письмо как обработанное
            if self.auto_archive:
                self.email_handler.archive_email(email_id)
            else:
                self.email_handler.mark_as_read(email_id)
            
            logger.info(f"Письмо {email_id} успешно обработано")
            return True
            
        except Exception as e:
            error_msg = f"Неожиданная ошибка при обработке письма: {e}"
            logger.error(error_msg, exc_info=True)
            self._notify_error(email_info, error_msg)
            return False
    
    def _validate_request(self, request) -> bool:
        """
        Валидация распарсенной заявки
        
        Проверяет наличие обязательных полей
        """
        # Минимальные требования: должен быть адрес забора и доставки
        if not request.pickup.address or not request.delivery.address:
            return False
        
        # Дополнительные проверки можно добавить здесь
        return True
    
    def _prepare_order_data(self, request_dict: Dict, email_info: Dict) -> Dict:
        """
        Подготовка данных для отправки в 1С:УАТ
        
        Преобразует данные из формата парсера в формат API 1С:УАТ
        """
        pickup = request_dict.get('pickup', {})
        delivery = request_dict.get('delivery', {})
        cargo = request_dict.get('cargo', {})
        vehicle = request_dict.get('vehicle', {})
        conditions = request_dict.get('conditions', {})
        
        # Генерируем ID заявки
        request_id = f"EMAIL-{email_info.get('id', datetime.now().strftime('%Y%m%d%H%M%S'))}"
        
        # Формируем datetime для заявки
        pickup_date = pickup.get('date', '')
        pickup_time = pickup.get('time', '00:00')
        datetime_str = f"{pickup_date}T{pickup_time}:00" if pickup_date else datetime.now().isoformat()
        
        # Извлекаем локации из адресов (можно улучшить с помощью геокодинга)
        from_location = self._extract_location_name(pickup.get('address', ''))
        to_location = self._extract_location_name(delivery.get('address', ''))
        
        order_data = {
            'request_id': request_id,
            'datetime': datetime_str,
            'from_location': from_location or 'Не указано',
            'to_location': to_location or 'Не указано',
            'pickup_address': pickup.get('address', ''),
            'delivery_address': delivery.get('address', ''),
            'pickup_date': pickup.get('date', ''),
            'pickup_time': pickup.get('time', '00:00'),
            'delivery_date': delivery.get('date', ''),
            'delivery_time': delivery.get('time', '00:00'),
            'cargo_name': cargo.get('name', ''),
            'cargo_weight': cargo.get('weight', ''),
            'cargo_volume': cargo.get('volume', ''),
            'vehicle_type': 'Автомобильная грузовая',  # Можно определить по типу кузова
            'body_type': cargo.get('body_type', ''),
            'loading_type': cargo.get('loading_type', ''),
            'contact_person': pickup.get('contact_person', ''),
            'contact_phone': pickup.get('contact_phone', ''),
            'driver': vehicle.get('driver', ''),
            'car': vehicle.get('car', ''),
            'car_number': vehicle.get('car_number', ''),
            'trailer_number': vehicle.get('trailer_number', ''),
            'body_state': conditions.get('body_state', ''),
            'payment': conditions.get('payment', ''),
            'documents': conditions.get('documents', '')
        }
        
        return order_data
    
    def _extract_location_name(self, address: str) -> Optional[str]:
        """
        Извлечение названия локации из адреса
        
        Простая реализация - можно улучшить с помощью геокодинга
        """
        if not address:
            return None
        
        # Пытаемся найти название города/населенного пункта
        # Обычно это первая часть адреса до запятой
        parts = address.split(',')
        if len(parts) > 0:
            # Берем первую часть, убираем лишние слова
            location = parts[0].strip()
            # Убираем "Московская область" и подобное
            location = location.replace('Московская область', '').strip()
            location = location.replace('г.', '').strip()
            location = location.replace('р.п.', '').strip()
            return location if location else None
        
        return None
    
    def _notify_error(self, email_info: Dict, error_msg: str):
        """Отправить уведомление об ошибке администратору"""
        if not self.notify_on_error or not self.admin_email:
            return
        
        try:
            subject = f"Ошибка обработки заявки: {email_info.get('subject', 'Без темы')}"
            body = f"""
Произошла ошибка при обработке заявки:

Письмо:
- От: {email_info.get('sender', 'Неизвестно')}
- Тема: {email_info.get('subject', 'Без темы')}
- Дата: {email_info.get('date', 'Неизвестно')}

Ошибка:
{error_msg}

Текст письма:
{email_info.get('body', '')[:500]}...
"""
            self.email_handler.send_email(
                to=self.admin_email,
                subject=subject,
                body=body,
                is_html=False
            )
        except Exception as e:
            logger.error(f"Ошибка отправки уведомления администратору: {e}")
    
    def run_once(self) -> int:
        """
        Выполнить одну проверку новых писем
        
        Returns:
            Количество обработанных писем
        """
        logger.info("=" * 60)
        logger.info("Начало проверки новых заявок")
        logger.info("=" * 60)
        
        # Получаем непрочитанные письма
        emails = self.email_handler.get_unread_emails(limit=self.max_emails_per_check)
        
        if not emails:
            logger.info("Новых писем не найдено")
            return 0
        
        logger.info(f"Найдено новых писем: {len(emails)}")
        
        processed = 0
        errors = 0
        
        for email_info in emails:
            if self.process_email(email_info):
                processed += 1
            else:
                errors += 1
        
        logger.info("=" * 60)
        logger.info(f"Обработка завершена: успешно {processed}, ошибок {errors}")
        logger.info("=" * 60)
        
        return processed
    
    def run_continuous(self):
        """Запуск непрерывной обработки (цикл проверки)"""
        logger.info("Запуск непрерывной обработки заявок")
        logger.info(f"Интервал проверки: {self.check_interval} секунд")
        
        try:
            while True:
                self.run_once()
                logger.info(f"Ожидание {self.check_interval} секунд до следующей проверки...")
                time.sleep(self.check_interval)
        except KeyboardInterrupt:
            logger.info("Остановка обработки по запросу пользователя")
        except Exception as e:
            logger.error(f"Критическая ошибка: {e}", exc_info=True)


def load_config(config_path: str = 'transport_config.json') -> Dict:
    """Загрузка конфигурации из файла"""
    try:
        with open(config_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except FileNotFoundError:
        logger.warning(f"Файл конфигурации {config_path} не найден. Используются значения по умолчанию.")
        return {}
    except json.JSONDecodeError as e:
        logger.error(f"Ошибка парсинга конфигурации: {e}")
        return {}


def main():
    """Главная функция"""
    import argparse
    
    parser = argparse.ArgumentParser(description='Автоматизация обработки заявок на перевозку')
    parser.add_argument('--config', '-c', default='transport_config.json',
                       help='Путь к файлу конфигурации')
    parser.add_argument('--once', action='store_true',
                       help='Выполнить одну проверку и завершить')
    parser.add_argument('--interval', '-i', type=int, default=None,
                       help='Интервал проверки в секундах (для непрерывного режима)')
    
    args = parser.parse_args()
    
    # Загружаем конфигурацию
    config = load_config(args.config)
    
    if not config:
        logger.error("Конфигурация не загружена. Создайте файл transport_config.json")
        sys.exit(1)
    
    # Создаем экземпляр автоматизации
    automation = TransportAutomation(config)
    
    # Устанавливаем интервал, если указан
    if args.interval:
        automation.check_interval = args.interval
    
    # Запускаем обработку
    if args.once:
        automation.run_once()
    else:
        automation.run_continuous()


if __name__ == "__main__":
    main()

