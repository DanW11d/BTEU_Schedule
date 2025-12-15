"""
Модуль для формирования и отправки заданий водителю
"""

import logging
from typing import Dict, Optional
from datetime import datetime

logger = logging.getLogger(__name__)


class DriverNotifier:
    """Класс для формирования и отправки заданий водителю"""
    
    def __init__(self, email_handler, config: Dict):
        """
        Инициализация уведомителя водителя
        
        Args:
            email_handler: Экземпляр EmailHandler для отправки писем
            config: Словарь с настройками:
                - driver_email: email для отправки заданий водителю
                - email_template: шаблон письма (опционально)
                - use_html: использовать HTML формат (True/False)
        """
        self.email_handler = email_handler
        self.driver_email = config.get('driver_email', '')
        self.use_html = config.get('use_html', False)
        self.email_template = config.get('email_template', None)
    
    def format_driver_task(self, request_data: Dict, order_id: Optional[str] = None) -> str:
        """
        Форматирование задания для водителя в читаемый вид
        
        Args:
            request_data: Данные заявки (результат парсинга)
            order_id: Номер заявки в системе (если есть)
        
        Returns:
            Отформатированный текст задания
        """
        pickup = request_data.get('pickup', {})
        delivery = request_data.get('delivery', {})
        cargo = request_data.get('cargo', {})
        vehicle = request_data.get('vehicle', {})
        conditions = request_data.get('conditions', {})
        
        if self.use_html:
            return self._format_html_task(pickup, delivery, cargo, vehicle, conditions, order_id)
        else:
            return self._format_text_task(pickup, delivery, cargo, vehicle, conditions, order_id)
    
    def _format_text_task(self, pickup: Dict, delivery: Dict, cargo: Dict, 
                         vehicle: Dict, conditions: Dict, order_id: Optional[str]) -> str:
        """Форматирование в текстовом формате"""
        lines = []
        
        if order_id:
            lines.append(f"ЗАЯВКА № {order_id}")
            lines.append("=" * 60)
            lines.append("")
        
        lines.append("ЗАДАЧА ДЛЯ ВОДИТЕЛЯ")
        lines.append("")
        lines.append("Маршрут и график:")
        lines.append("")
        
        # Забор груза
        lines.append("1. Забор груза:")
        lines.append("")
        if pickup.get('date'):
            lines.append(f"   · Дата и время: {pickup.get('date')} {pickup.get('time', '')}".strip())
        if pickup.get('address'):
            lines.append(f"   · Адрес: {pickup.get('address')}")
        if pickup.get('contact_person'):
            contact_line = f"   · Контактное лицо: {pickup.get('contact_person')}"
            if pickup.get('contact_phone'):
                contact_line += f", тел.: {pickup.get('contact_phone')}"
            lines.append(contact_line)
        lines.append("")
        
        # Доставка
        lines.append("2. Доставка и выгрузка:")
        lines.append("")
        if delivery.get('date'):
            lines.append(f"   · Дата и время: {delivery.get('date')} {delivery.get('time', '')}".strip())
        if delivery.get('address'):
            lines.append(f"   · Адрес: {delivery.get('address')}")
        lines.append("")
        
        # Информация о грузе
        lines.append("Информация о грузе:")
        lines.append("")
        if cargo.get('name'):
            lines.append(f"· Наименование: {cargo.get('name')}")
        if cargo.get('weight') or cargo.get('volume'):
            weight_volume = []
            if cargo.get('weight'):
                weight_volume.append(cargo.get('weight'))
            if cargo.get('volume'):
                weight_volume.append(cargo.get('volume'))
            lines.append(f"· Вес/Объем: {' / '.join(weight_volume)}")
        if cargo.get('body_type') or cargo.get('loading_type'):
            body_loading = []
            if cargo.get('body_type'):
                body_loading.append(cargo.get('body_type'))
            if cargo.get('loading_type'):
                body_loading.append(f"погрузка и выгрузка — {cargo.get('loading_type')}")
            lines.append(f"· Тип кузова/погрузки: {', '.join(body_loading)}")
        lines.append("")
        
        # Транспорт
        lines.append("Транспортное средство и водитель:")
        lines.append("")
        if vehicle.get('car') or vehicle.get('car_number'):
            car_line = "· Автомобиль: "
            if vehicle.get('car'):
                car_line += vehicle.get('car')
            if vehicle.get('car_number'):
                car_line += f", гос. номер: {vehicle.get('car_number')}"
            lines.append(car_line)
        if vehicle.get('trailer_number'):
            lines.append(f"· Прицеп: Гос. номер: {vehicle.get('trailer_number')}")
        if vehicle.get('driver'):
            lines.append(f"· Водитель: {vehicle.get('driver')}")
        lines.append("")
        
        # Условия
        if conditions.get('body_state') or conditions.get('payment') or conditions.get('documents'):
            lines.append("Ключевые требования и условия:")
            lines.append("")
            if conditions.get('body_state'):
                lines.append(f"· Состояние кузова: {conditions.get('body_state')}")
            if conditions.get('payment'):
                lines.append(f"· Оплата: {conditions.get('payment')}")
            if conditions.get('documents'):
                lines.append(f"· Для оплаты необходимо предоставить оригиналы документов: {conditions.get('documents')}")
        
        return "\n".join(lines)
    
    def _format_html_task(self, pickup: Dict, delivery: Dict, cargo: Dict,
                         vehicle: Dict, conditions: Dict, order_id: Optional[str]) -> str:
        """Форматирование в HTML формате"""
        html = f"""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body {{ font-family: Arial, sans-serif; line-height: 1.6; color: #333; }}
                .header {{ background-color: #4CAF50; color: white; padding: 15px; text-align: center; }}
                .section {{ margin: 20px 0; padding: 15px; background-color: #f9f9f9; border-left: 4px solid #4CAF50; }}
                .section-title {{ font-weight: bold; color: #4CAF50; margin-bottom: 10px; }}
                .field {{ margin: 8px 0; }}
                .field-label {{ font-weight: bold; }}
                .conditions {{ background-color: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; }}
            </style>
        </head>
        <body>
        """
        
        if order_id:
            html += f'<div class="header"><h1>ЗАЯВКА № {order_id}</h1></div>'
        
        html += '<h2>ЗАДАЧА ДЛЯ ВОДИТЕЛЯ</h2>'
        
        # Забор груза
        html += '<div class="section">'
        html += '<div class="section-title">1. Забор груза:</div>'
        if pickup.get('date'):
            html += f'<div class="field"><span class="field-label">Дата и время:</span> {pickup.get("date")} {pickup.get("time", "")}</div>'
        if pickup.get('address'):
            html += f'<div class="field"><span class="field-label">Адрес:</span> {pickup.get("address")}</div>'
        if pickup.get('contact_person'):
            contact = pickup.get('contact_person')
            if pickup.get('contact_phone'):
                contact += f', тел.: {pickup.get("contact_phone")}'
            html += f'<div class="field"><span class="field-label">Контактное лицо:</span> {contact}</div>'
        html += '</div>'
        
        # Доставка
        html += '<div class="section">'
        html += '<div class="section-title">2. Доставка и выгрузка:</div>'
        if delivery.get('date'):
            html += f'<div class="field"><span class="field-label">Дата и время:</span> {delivery.get("date")} {delivery.get("time", "")}</div>'
        if delivery.get('address'):
            html += f'<div class="field"><span class="field-label">Адрес:</span> {delivery.get("address")}</div>'
        html += '</div>'
        
        # Груз
        html += '<div class="section">'
        html += '<div class="section-title">Информация о грузе:</div>'
        if cargo.get('name'):
            html += f'<div class="field"><span class="field-label">Наименование:</span> {cargo.get("name")}</div>'
        if cargo.get('weight') or cargo.get('volume'):
            weight_volume = []
            if cargo.get('weight'):
                weight_volume.append(cargo.get('weight'))
            if cargo.get('volume'):
                weight_volume.append(cargo.get('volume'))
            html += f'<div class="field"><span class="field-label">Вес/Объем:</span> {" / ".join(weight_volume)}</div>'
        if cargo.get('body_type') or cargo.get('loading_type'):
            body_loading = []
            if cargo.get('body_type'):
                body_loading.append(cargo.get('body_type'))
            if cargo.get('loading_type'):
                body_loading.append(f"погрузка и выгрузка — {cargo.get('loading_type')}")
            html += f'<div class="field"><span class="field-label">Тип кузова/погрузки:</span> {", ".join(body_loading)}</div>'
        html += '</div>'
        
        # Транспорт
        html += '<div class="section">'
        html += '<div class="section-title">Транспортное средство и водитель:</div>'
        if vehicle.get('car') or vehicle.get('car_number'):
            car = vehicle.get('car', '')
            if vehicle.get('car_number'):
                car += f', гос. номер: {vehicle.get("car_number")}'
            html += f'<div class="field"><span class="field-label">Автомобиль:</span> {car}</div>'
        if vehicle.get('trailer_number'):
            html += f'<div class="field"><span class="field-label">Прицеп:</span> Гос. номер: {vehicle.get("trailer_number")}</div>'
        if vehicle.get('driver'):
            html += f'<div class="field"><span class="field-label">Водитель:</span> {vehicle.get("driver")}</div>'
        html += '</div>'
        
        # Условия
        if conditions.get('body_state') or conditions.get('payment') or conditions.get('documents'):
            html += '<div class="conditions">'
            html += '<div class="section-title">Ключевые требования и условия:</div>'
            if conditions.get('body_state'):
                html += f'<div class="field"><span class="field-label">Состояние кузова:</span> {conditions.get("body_state")}</div>'
            if conditions.get('payment'):
                html += f'<div class="field"><span class="field-label">Оплата:</span> {conditions.get("payment")}</div>'
            if conditions.get('documents'):
                html += f'<div class="field"><span class="field-label">Документы:</span> {conditions.get("documents")}</div>'
            html += '</div>'
        
        html += """
        </body>
        </html>
        """
        
        return html
    
    def send_driver_task(self, request_data: Dict, order_id: Optional[str] = None) -> bool:
        """
        Отправить задание водителю
        
        Args:
            request_data: Данные заявки
            order_id: Номер заявки в системе
        
        Returns:
            True если отправка успешна, False иначе
        """
        if not self.driver_email:
            logger.error("Email водителя не указан в конфигурации")
            return False
        
        # Формируем текст задания
        task_text = self.format_driver_task(request_data, order_id)
        
        # Формируем тему письма
        subject = f"Новая задача для водителя"
        if order_id:
            subject += f" (Заявка №{order_id})"
        
        # Отправляем email
        success = self.email_handler.send_email(
            to=self.driver_email,
            subject=subject,
            body=task_text,
            is_html=self.use_html
        )
        
        if success:
            logger.info(f"Задание отправлено водителю: {self.driver_email}")
        else:
            logger.error(f"Ошибка отправки задания водителю: {self.driver_email}")
        
        return success


# Пример использования
if __name__ == "__main__":
    # Настройка логирования
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # Пример данных заявки
    request_data = {
        'pickup': {
            'date': '2025-11-20',
            'time': '10:00',
            'address': 'Московская область, г.о. Люберцы, ул. Ленина, д. 93',
            'contact_person': 'Дарая',
            'contact_phone': '8-858-254-09-04'
        },
        'delivery': {
            'date': '2025-11-20',
            'time': '14:00',
            'address': 'Московская область, р.п. Обухово, дом 36, ворота 54'
        },
        'cargo': {
            'name': 'Мебель',
            'weight': '20 тонн',
            'volume': '1 м³',
            'body_type': 'Тент',
            'loading_type': 'задняя'
        },
        'vehicle': {
            'car': 'Ford F MAX',
            'car_number': 'A992X0750',
            'trailer_number': 'EK603650',
            'driver': 'Наумов Валерий Станиславович'
        },
        'conditions': {
            'body_state': 'Кузов должен быть СУХИМ, ЧИСТЫМ и БЕЗ ПРОТЕЧЕК!',
            'payment': 'Безналичный расчет по факту выгрузки',
            'documents': 'акт выполненных работ и транспортную накладную'
        }
    }
    
    # Для теста создаем mock email handler
    class MockEmailHandler:
        def send_email(self, to, subject, body, is_html=False):
            print(f"\n{'='*60}")
            print(f"ОТПРАВКА EMAIL")
            print(f"{'='*60}")
            print(f"Кому: {to}")
            print(f"Тема: {subject}")
            print(f"HTML: {is_html}")
            print(f"\n{body}")
            return True
    
    mock_handler = MockEmailHandler()
    
    config = {
        'driver_email': 'driver@example.com',
        'use_html': False
    }
    
    notifier = DriverNotifier(mock_handler, config)
    notifier.send_driver_task(request_data, order_id='EMAIL-001')

