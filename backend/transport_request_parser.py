"""
Парсер заявок на перевозку из email
Обрабатывает стандартизированный формат заявок
"""

import re
from datetime import datetime
from typing import Dict, Optional, List
from dataclasses import dataclass


@dataclass
class PickupInfo:
    """Информация о заборе груза"""
    date: Optional[str] = None
    time: Optional[str] = None
    address: Optional[str] = None
    contact_person: Optional[str] = None
    contact_phone: Optional[str] = None


@dataclass
class DeliveryInfo:
    """Информация о доставке"""
    date: Optional[str] = None
    time: Optional[str] = None
    address: Optional[str] = None


@dataclass
class CargoInfo:
    """Информация о грузе"""
    name: Optional[str] = None
    weight: Optional[str] = None
    volume: Optional[str] = None
    body_type: Optional[str] = None
    loading_type: Optional[str] = None


@dataclass
class VehicleInfo:
    """Информация о транспорте и водителе"""
    car: Optional[str] = None
    car_number: Optional[str] = None
    trailer: Optional[str] = None
    trailer_number: Optional[str] = None
    driver: Optional[str] = None


@dataclass
class ConditionsInfo:
    """Дополнительные условия"""
    body_state: Optional[str] = None
    payment: Optional[str] = None
    documents: Optional[str] = None


@dataclass
class TransportRequest:
    """Полная структура заявки на перевозку"""
    pickup: PickupInfo
    delivery: DeliveryInfo
    cargo: CargoInfo
    vehicle: VehicleInfo
    conditions: ConditionsInfo
    raw_text: str  # Исходный текст для отладки


class TransportRequestParser:
    """Парсер заявок на перевозку"""
    
    def __init__(self):
        # Паттерны для извлечения данных
        self.patterns = {
            # Дата и время
            'date_time': re.compile(r'Дата и время:\s*(.+?)(?:\.|$)', re.IGNORECASE | re.MULTILINE),
            'date': re.compile(r'(\d{1,2})\s+(ноября|декабря|января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября)\s+(\d{4})\s+года', re.IGNORECASE),
            
            # Адрес (захватываем до следующего пункта списка или конца строки)
            'address': re.compile(r'Адрес:\s*(.+?)(?:\n\s*·|$)', re.IGNORECASE | re.MULTILINE | re.DOTALL),
            
            # Контактное лицо
            'contact_person': re.compile(r'Контактное лицо:\s*(.+?)(?:,|\.|тел)', re.IGNORECASE | re.MULTILINE),
            'contact_phone': re.compile(r'тел\.?:\s*([\d\s\-\(\)\+]+)', re.IGNORECASE),
            
            # Груз
            'cargo_name': re.compile(r'Наименование:\s*(.+?)(?:\.|$)', re.IGNORECASE | re.MULTILINE),
            'weight_volume': re.compile(r'Вес/Объем:\s*(.+?)(?:\.|$)', re.IGNORECASE | re.MULTILINE),
            'body_type_loading': re.compile(r'Тип кузова/погрузки:\s*(.+?)(?:\.|$)', re.IGNORECASE | re.MULTILINE),
            
            # Транспорт
            'car': re.compile(r'Автомобиль:\s*(.+?)(?:,|\.|гос)', re.IGNORECASE | re.MULTILINE),
            'car_number': re.compile(r'гос\.?\s*номер:\s*([A-ZА-Я0-9]+)', re.IGNORECASE),
            'trailer': re.compile(r'Прицеп:\s*(?:Гос\.?\s*номер:\s*)?([A-ZА-Я0-9]+)', re.IGNORECASE),
            'driver': re.compile(r'Водитель:\s*(.+?)(?:\.|$)', re.IGNORECASE | re.MULTILINE),
            
            # Условия
            'body_state': re.compile(r'Состояние кузова:\s*(.+?)(?:\.|$)', re.IGNORECASE | re.MULTILINE),
            'payment': re.compile(r'Оплата:\s*(.+?)(?:\.|$)', re.IGNORECASE | re.MULTILINE),
            'documents': re.compile(r'документов:\s*(.+?)(?:\.|$)', re.IGNORECASE | re.MULTILINE),
        }
        
        # Месяцы для парсинга даты
        self.months = {
            'января': 1, 'февраля': 2, 'марта': 3, 'апреля': 4,
            'мая': 5, 'июня': 6, 'июля': 7, 'августа': 8,
            'сентября': 9, 'октября': 10, 'ноября': 11, 'декабря': 12
        }
    
    def parse_date(self, date_str: str) -> Optional[str]:
        """Парсит дату в формате '20 ноября 2025 года' в ISO формат"""
        if not date_str:
            return None
        
        # Пытаемся найти дату в тексте
        match = self.patterns['date'].search(date_str)
        if match:
            day = int(match.group(1))
            month_name = match.group(2).lower()
            year = int(match.group(3))
            
            if month_name in self.months:
                month = self.months[month_name]
                return f"{year}-{month:02d}-{day:02d}"
        
        return None
    
    def extract_section(self, text: str, section_name: str, next_section: Optional[str] = None) -> str:
        """Извлекает текст раздела"""
        # Ищем начало раздела
        start_pattern = re.compile(f'{section_name}.*?:(?:\\n|$)', re.IGNORECASE | re.MULTILINE)
        start_match = start_pattern.search(text)
        
        if not start_match:
            return ""
        
        start_pos = start_match.end()
        
        # Ищем конец раздела (начало следующего или конец текста)
        if next_section:
            end_pattern = re.compile(f'{next_section}.*?:(?:\\n|$)', re.IGNORECASE | re.MULTILINE)
            end_match = end_pattern.search(text, start_pos)
            if end_match:
                return text[start_pos:end_match.start()].strip()
        
        return text[start_pos:].strip()
    
    def parse_pickup(self, text: str) -> PickupInfo:
        """Парсит информацию о заборе груза"""
        pickup = PickupInfo()
        
        # Извлекаем раздел "Забор груза"
        pickup_section = self.extract_section(text, 'Забор груза', 'Доставка')
        
        # Дата и время
        date_time_match = self.patterns['date_time'].search(pickup_section)
        if date_time_match:
            date_time_str = date_time_match.group(1).strip()
            pickup.date = self.parse_date(date_time_str)
            # Время пока не парсим отдельно, можно добавить позже
        
        # Адрес
        address_match = self.patterns['address'].search(pickup_section)
        if address_match:
            address = address_match.group(1).strip()
            # Убираем лишние переносы строк и пробелы
            address = ' '.join(address.split())
            # Убираем точку в конце, если есть
            if address.endswith('.'):
                address = address[:-1]
            pickup.address = address
        
        # Контактное лицо
        contact_match = self.patterns['contact_person'].search(pickup_section)
        if contact_match:
            pickup.contact_person = contact_match.group(1).strip()
        
        # Телефон
        phone_match = self.patterns['contact_phone'].search(pickup_section)
        if phone_match:
            pickup.contact_phone = phone_match.group(1).strip()
        
        return pickup
    
    def parse_delivery(self, text: str) -> DeliveryInfo:
        """Парсит информацию о доставке"""
        delivery = DeliveryInfo()
        
        # Извлекаем раздел "Доставка"
        delivery_section = self.extract_section(text, 'Доставка', 'Информация о грузе')
        
        # Дата и время
        date_time_match = self.patterns['date_time'].search(delivery_section)
        if date_time_match:
            date_time_str = date_time_match.group(1).strip()
            delivery.date = self.parse_date(date_time_str)
        
        # Адрес
        address_match = self.patterns['address'].search(delivery_section)
        if address_match:
            address = address_match.group(1).strip()
            # Убираем лишние переносы строк и пробелы
            address = ' '.join(address.split())
            # Убираем точку в конце, если есть
            if address.endswith('.'):
                address = address[:-1]
            delivery.address = address
        
        return delivery
    
    def parse_cargo(self, text: str) -> CargoInfo:
        """Парсит информацию о грузе"""
        cargo = CargoInfo()
        
        # Извлекаем раздел "Информация о грузе"
        cargo_section = self.extract_section(text, 'Информация о грузе', 'Транспортное средство')
        
        # Наименование
        name_match = self.patterns['cargo_name'].search(cargo_section)
        if name_match:
            cargo.name = name_match.group(1).strip()
        
        # Вес/Объем
        weight_volume_match = self.patterns['weight_volume'].search(cargo_section)
        if weight_volume_match:
            weight_volume_str = weight_volume_match.group(1).strip()
            # Разделяем вес и объем
            parts = weight_volume_str.split('/')
            if len(parts) >= 1:
                cargo.weight = parts[0].strip()
            if len(parts) >= 2:
                cargo.volume = parts[1].strip()
        
        # Тип кузова/погрузки
        body_type_match = self.patterns['body_type_loading'].search(cargo_section)
        if body_type_match:
            body_type_str = body_type_match.group(1).strip()
            # Разделяем тип кузова и тип погрузки
            if ',' in body_type_str:
                parts = body_type_str.split(',')
                cargo.body_type = parts[0].strip()
                cargo.loading_type = ','.join(parts[1:]).strip()
            else:
                cargo.body_type = body_type_str
        
        return cargo
    
    def parse_vehicle(self, text: str) -> VehicleInfo:
        """Парсит информацию о транспорте и водителе"""
        vehicle = VehicleInfo()
        
        # Извлекаем раздел "Транспортное средство"
        vehicle_section = self.extract_section(text, 'Транспортное средство', 'Ключевые требования')
        
        # Автомобиль
        car_match = self.patterns['car'].search(vehicle_section)
        if car_match:
            vehicle.car = car_match.group(1).strip()
        
        # Гос. номер автомобиля
        car_number_match = self.patterns['car_number'].search(vehicle_section)
        if car_number_match:
            vehicle.car_number = car_number_match.group(1).strip()
        
        # Прицеп - ищем строку "Прицеп: Гос. номер: EK603650"
        trailer_match = self.patterns['trailer'].search(vehicle_section)
        if trailer_match:
            vehicle.trailer_number = trailer_match.group(1).strip()
        
        # Водитель
        driver_match = self.patterns['driver'].search(vehicle_section)
        if driver_match:
            driver = driver_match.group(1).strip()
            # Убираем точку в конце, если есть
            if driver.endswith('.'):
                driver = driver[:-1]
            vehicle.driver = driver
        
        return vehicle
    
    def parse_conditions(self, text: str) -> ConditionsInfo:
        """Парсит дополнительные условия"""
        conditions = ConditionsInfo()
        
        # Извлекаем раздел "Ключевые требования"
        conditions_section = self.extract_section(text, 'Ключевые требования', 'Краткий план')
        
        # Состояние кузова
        body_state_match = self.patterns['body_state'].search(conditions_section)
        if body_state_match:
            conditions.body_state = body_state_match.group(1).strip()
        
        # Оплата
        payment_match = self.patterns['payment'].search(conditions_section)
        if payment_match:
            conditions.payment = payment_match.group(1).strip()
        
        # Документы
        documents_match = self.patterns['documents'].search(conditions_section)
        if documents_match:
            conditions.documents = documents_match.group(1).strip()
        
        return conditions
    
    def parse(self, email_text: str) -> TransportRequest:
        """Парсит полный текст email и возвращает структурированную заявку"""
        # Нормализуем текст (убираем лишние пробелы, приводим к единому формату)
        text = email_text.replace('\r\n', '\n').replace('\r', '\n')
        
        # Парсим все разделы
        pickup = self.parse_pickup(text)
        delivery = self.parse_delivery(text)
        cargo = self.parse_cargo(text)
        vehicle = self.parse_vehicle(text)
        conditions = self.parse_conditions(text)
        
        return TransportRequest(
            pickup=pickup,
            delivery=delivery,
            cargo=cargo,
            vehicle=vehicle,
            conditions=conditions,
            raw_text=email_text
        )
    
    def to_dict(self, request: TransportRequest) -> Dict:
        """Преобразует заявку в словарь для отправки в API"""
        return {
            "pickup": {
                "date": request.pickup.date,
                "time": request.pickup.time,
                "address": request.pickup.address,
                "contact_person": request.pickup.contact_person,
                "contact_phone": request.pickup.contact_phone
            },
            "delivery": {
                "date": request.delivery.date,
                "time": request.delivery.time,
                "address": request.delivery.address
            },
            "cargo": {
                "name": request.cargo.name,
                "weight": request.cargo.weight,
                "volume": request.cargo.volume,
                "body_type": request.cargo.body_type,
                "loading_type": request.cargo.loading_type
            },
            "vehicle": {
                "car": request.vehicle.car,
                "car_number": request.vehicle.car_number,
                "trailer": request.vehicle.trailer,
                "trailer_number": request.vehicle.trailer_number,
                "driver": request.vehicle.driver
            },
            "conditions": {
                "body_state": request.conditions.body_state,
                "payment": request.conditions.payment,
                "documents": request.conditions.documents
            }
        }


# Пример использования
if __name__ == "__main__":
    # Тестовый пример заявки
    sample_request = """
ЗАДАЧА ДЛЯ ВОДИТЕЛЯ

Маршрут и график:

1. Забор груза:

   · Дата и время: 20 ноября 2025 года.

   · Адрес: Московская область, г.о. Люберцы, р.п. Октябрьский, ул. Ленина, д. 93.

   · Контактное лицо: Дарая, тел.: 8-858-254-09-04.

2. Доставка и выгрузка:

   · Дата и время: 20 ноября 2025 года.

   · Адрес: Московская область, Богородский городской округ, р.п. Обухово, территория Атлант-Парк, дом 36, ворота 54.

Информация о грузе:

· Наименование: Мебель.

· Вес/Объем: 20 тонн / 1 м³.

· Тип кузова/погрузки: Тент, погрузка и выгрузка — задняя.

Транспортное средство и водитель:

· Автомобиль: Ford F MAX, гос. номер: A992X0750.

· Прицеп: Гос. номер: EK603650.

· Водитель: Наумов Валерий Станиславович.

Ключевые требования и условия:

· Состояние кузова: Кузов должен быть СУХИМ, ЧИСТЫМ и БЕЗ ПРОТЕЧЕК!

· Оплата: Безналичный расчет по факту выгрузки. Для оплаты необходимо предоставить оригиналы документов: акт выполненных работ и транспортную накладную.
"""
    
    parser = TransportRequestParser()
    request = parser.parse(sample_request)
    
    print("=" * 60)
    print("РЕЗУЛЬТАТ ПАРСИНГА")
    print("=" * 60)
    print(f"\nЗАБОР ГРУЗА:")
    print(f"  Дата: {request.pickup.date}")
    print(f"  Адрес: {request.pickup.address}")
    print(f"  Контакт: {request.pickup.contact_person}, {request.pickup.contact_phone}")
    
    print(f"\nДОСТАВКА:")
    print(f"  Дата: {request.delivery.date}")
    print(f"  Адрес: {request.delivery.address}")
    
    print(f"\nГРУЗ:")
    print(f"  Наименование: {request.cargo.name}")
    print(f"  Вес: {request.cargo.weight}, Объем: {request.cargo.volume}")
    print(f"  Тип кузова: {request.cargo.body_type}")
    print(f"  Тип погрузки: {request.cargo.loading_type}")
    
    print(f"\nТРАНСПОРТ:")
    print(f"  Автомобиль: {request.vehicle.car}, {request.vehicle.car_number}")
    print(f"  Прицеп: {request.vehicle.trailer_number}")
    print(f"  Водитель: {request.vehicle.driver}")
    
    print(f"\nУСЛОВИЯ:")
    print(f"  Состояние кузова: {request.conditions.body_state}")
    print(f"  Оплата: {request.conditions.payment}")
    print(f"  Документы: {request.conditions.documents}")
    
    print("\n" + "=" * 60)
    print("СЛОВАРЬ ДЛЯ API:")
    print("=" * 60)
    import json
    print(json.dumps(parser.to_dict(request), ensure_ascii=False, indent=2))

